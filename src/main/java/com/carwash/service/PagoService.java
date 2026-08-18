package com.carwash.service;

import com.carwash.dto.*;
import com.carwash.exception.*;
import com.carwash.model.*;
import com.carwash.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PagoService {
    private final PagoRepository pagoRepository;
    private final ReservaRepository reservaRepository;
    private final TarjetaCreditoService tarjetaService;
    private final CodigoYapeService codigoYapeService;
    private final ModelMapper modelMapper;
    
    // ========== NUEVAS DEPENDENCIAS PARA FIDELIZACIÓN ==========
    private final CuponRepository cuponRepository;
    private final PerfilClienteRepository perfilClienteRepository;

    @Transactional
    public PagoDTO crearPago(PagoCreateDTO createDTO) {
        Reserva reserva = reservaRepository.findById(createDTO.getReservaId())
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        // Verificar si ya existe un pago para esta reserva
        if (pagoRepository.findByReservaId(reserva.getId()).isPresent()) {
            throw new BusinessException("Ya existe un pago para esta reserva");
        }

        // Validar método de pago
        validarMetodoPago(createDTO, reserva.getCliente().getId());

        BigDecimal monto = reserva.getServicio().getPrecio();
        BigDecimal descuento = BigDecimal.ZERO;
        Long cuponAplicadoId = null;
        String cuponCodigo = null;
        Integer porcentajeDescuento = null;

        // Aplicar descuento de cupón si existe (SISTEMA ANTIGUO)
        if (reserva.getCupon() != null) {
            Cupon cupon = reserva.getCupon();
            if (cupon.getTipoDescuento() == TipoDescuento.PORCENTAJE) {
                descuento = monto.multiply(cupon.getValorDescuento())
                        .divide(BigDecimal.valueOf(100));
                porcentajeDescuento = cupon.getValorDescuento().intValue();
            } else {
                descuento = cupon.getValorDescuento();
            }
            cuponAplicadoId = cupon.getId();
            cuponCodigo = cupon.getCodigo();
            
            // Registrar uso del cupón
            cupon.registrarUso();
            cuponRepository.save(cupon);
        }

        BigDecimal montoTotal = monto.subtract(descuento);

        Pago pago = Pago.builder()
                .reserva(reserva)
                .cliente(reserva.getCliente())
                .monto(monto)
                .descuento(descuento)
                .montoTotal(montoTotal)
                .metodoPago(createDTO.getMetodoPago())
                .estado(EstadoPago.PROCESANDO)
                .fechaPago(LocalDateTime.now())
                .transaccionId(UUID.randomUUID().toString())
                .cuponAplicadoId(cuponAplicadoId)
                .cuponCodigo(cuponCodigo)
                .porcentajeDescuento(porcentajeDescuento)
                .puntosAcreditados(false)
                .puntosGanados(0)
                .build();

        pago = pagoRepository.save(pago);

        // Simular procesamiento de pago
        pago.setEstado(EstadoPago.COMPLETADO);
        pago = pagoRepository.save(pago);

        // Actualizar estado de reserva
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reservaRepository.save(reserva);
        
        // ========== NUEVO: ACREDITAR PUNTOS DE FIDELIZACIÓN ==========
        acreditarPuntosFidelizacion(pago);

        return modelMapper.map(pago, PagoDTO.class);
    }

    private void validarMetodoPago(PagoCreateDTO createDTO, Long usuarioId) {
        switch (createDTO.getMetodoPago()) {
            case TARJETA_CREDITO:
            case TARJETA_DEBITO:
                if (createDTO.getNumeroTarjeta() == null ||
                        createDTO.getCvv() == null ||
                        createDTO.getFechaExpiracion() == null) {
                    throw new BusinessException("Datos de tarjeta incompletos");
                }

                if (!tarjetaService.validarTarjeta(
                        createDTO.getNumeroTarjeta(),
                        createDTO.getCvv(),
                        createDTO.getFechaExpiracion())) {
                    throw new BusinessException("Tarjeta inválida o no registrada");
                }
                break;

            case YAPE:
            case PLIN:
                if (createDTO.getNumeroTelefono() == null || createDTO.getCodigo() == null) {
                    throw new BusinessException("Datos de " + createDTO.getMetodoPago() + " incompletos");
                }

                try {
                    boolean codigoValido = codigoYapeService.validarYUsarCodigo(
                            usuarioId,
                            createDTO.getCodigo(),
                            createDTO.getNumeroTelefono());
                    if (!codigoValido) {
                        throw new BusinessException("Código inválido");
                    }
                } catch (BusinessException e) {
                    throw e;
                }
                break;

            case EFECTIVO:
                // No requiere validación adicional
                break;

            default:
                throw new BusinessException("Método de pago no soportado");
        }
    }
    
    // ========== NUEVO MÉTODO: ACREDITAR PUNTOS DE FIDELIZACIÓN ==========
    
    /**
     * Acredita puntos de fidelización al cliente después de un pago exitoso
     * Regla: 1 Sol = 10 puntos
     */
    @Transactional
    protected void acreditarPuntosFidelizacion(Pago pago) {
        try {
            if (pago.getEstado() != EstadoPago.COMPLETADO) {
                return; // Solo acreditar si el pago fue exitoso
            }
            
            if (pago.getPuntosAcreditados()) {
                return; // Ya se acreditaron los puntos
            }
            
            Usuario cliente = pago.getCliente();
            PerfilCliente perfil = perfilClienteRepository.findByUsuario(cliente)
                    .orElse(null);
            
            if (perfil == null) {
                log.warn("No se encontró perfil para el cliente {}, creando uno nuevo", cliente.getId());
                perfil = PerfilCliente.builder()
                        .usuario(cliente)
                        .puntosFidelidad(0)
                        .totalServicios(0)
                        .totalGastado(BigDecimal.ZERO)
                        .build();
            }
            
            // Acreditar puntos basado en el monto PAGADO (no el original)
            BigDecimal montoPagado = pago.getMontoTotal();
            int puntosAnteriores = perfil.getPuntosFidelidad();
            
            perfil.agregarPuntosPorCompra(montoPagado);
            perfilClienteRepository.save(perfil);
            
            int puntosGanados = perfil.getPuntosFidelidad() - puntosAnteriores;
            
            // Actualizar el pago con los puntos ganados
            pago.setPuntosAcreditados(true);
            pago.setPuntosGanados(puntosGanados);
            pagoRepository.save(pago);
            
            log.info("✅ Cliente {} ganó {} puntos por pago de S/{}. Total acumulado: {} puntos", 
                    cliente.getEmail(), puntosGanados, montoPagado, perfil.getPuntosFidelidad());
            
        } catch (Exception e) {
            log.error("❌ Error al acreditar puntos de fidelización para pago {}: {}", 
                    pago.getId(), e.getMessage(), e);
            // No lanzar excepción para no afectar el flujo principal del pago
        }
    }

    public PagoDTO obtenerPorId(Long id) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado"));
        return modelMapper.map(pago, PagoDTO.class);
    }

    public PagoDTO obtenerPorReserva(Long reservaId) {
        Pago pago = pagoRepository.findByReservaId(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado"));
        return modelMapper.map(pago, PagoDTO.class);
    }

    public List<PagoDTO> listarTodos() {
        return pagoRepository.findAll().stream()
                .map(p -> modelMapper.map(p, PagoDTO.class))
                .collect(Collectors.toList());
    }

    public BigDecimal calcularIngresosHoy() {
        BigDecimal ingresos = pagoRepository.calcularIngresosDia(LocalDateTime.now());
        return ingresos != null ? ingresos : BigDecimal.ZERO;
    }

    public BigDecimal calcularIngresosMes() {
        LocalDateTime inicioMes = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        LocalDateTime finMes = LocalDateTime.now();
        BigDecimal ingresos = pagoRepository.calcularIngresosPeriodo(inicioMes, finMes);
        return ingresos != null ? ingresos : BigDecimal.ZERO;
    }
}