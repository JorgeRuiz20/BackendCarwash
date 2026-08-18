package com.carwash.service;

import com.carwash.dto.*;
import com.carwash.model.*;
import com.carwash.repository.*;
import com.carwash.exception.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservaService {
    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ServicioRepository servicioRepository;
    private final BahiaRepository bahiaRepository;
    private final CuponRepository cuponRepository;
    private final PerfilClienteRepository perfilClienteRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public ReservaDTO crearReserva(Long clienteId, ReservaCreateDTO createDTO) {
        Usuario cliente = usuarioRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        Servicio servicio = servicioRepository.findById(createDTO.getServicioId())
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        // NO asignar bahía aquí: la bahía se asigna cuando la reserva pasa a
        // EN_PROGRESO

        // Validar cupón si existe
        Cupon cupon = null;
        if (createDTO.getCodigoCupon() != null && !createDTO.getCodigoCupon().isEmpty()) {
            cupon = cuponRepository.findCuponValido(
                    createDTO.getCodigoCupon(),
                    LocalDateTime.now()).orElseThrow(() -> new BusinessException("Cupón inválido o expirado"));
        }

        BigDecimal precioBase = servicio.getPrecio();
        BigDecimal descuento = BigDecimal.ZERO;

        if (cupon != null) {
            if (cupon.getTipoDescuento() == TipoDescuento.PORCENTAJE) {
                descuento = precioBase.multiply(cupon.getValorDescuento())
                        .divide(BigDecimal.valueOf(100));
            } else {
                descuento = cupon.getValorDescuento();
            }
        }

        BigDecimal precioTotal = precioBase.subtract(descuento);

        Reserva reserva = Reserva.builder()
                .cliente(cliente)
                .servicio(servicio)
                .bahia(null)
                .fechaReserva(createDTO.getFechaReserva())
                .estado(EstadoReserva.PENDIENTE)
                .vehiculoPlaca(createDTO.getVehiculoPlaca())
                .vehiculoModelo(createDTO.getVehiculoModelo())
                .cupon(cupon)
                .precioServicio(precioBase)
                .descuentoAplicado(descuento)
                .precioTotal(precioTotal)
                .observaciones(createDTO.getObservaciones())
                .build();

        reserva = reservaRepository.save(reserva);

        // Actualizar uso de cupón
        if (cupon != null) {
            cupon.setUsosActuales(cupon.getUsosActuales() + 1);
            cuponRepository.save(cupon);
        }

        return convertirADTO(reserva);
    }

    public ReservaDTO obtenerPorId(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
        return convertirADTO(reserva);
    }

    public List<ReservaDTO> listarPorCliente(Long clienteId) {
        return reservaRepository.findByClienteIdOrderByFechaDesc(clienteId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public List<ReservaDTO> listarPorEstado(EstadoReserva estado) {
        return reservaRepository.findByEstadoOrderByFecha(estado).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public List<ReservaDTO> listarReservasHoy() {
        LocalDateTime hoy = LocalDateTime.now();
        return reservaRepository.findReservasActivasDelDia(hoy).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReservaDTO actualizarEstado(Long id, EstadoReserva nuevoEstado) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        reserva.setEstado(nuevoEstado);

        if (nuevoEstado == EstadoReserva.EN_PROGRESO) {
            reserva.setFechaInicio(LocalDateTime.now());
            // Asignar bahía si aún no tiene (se asigna al iniciar el servicio)
            if (reserva.getBahia() == null) {
                List<Bahia> bahiasDisponibles = bahiaRepository.findBahiasDisponibles();
                if (bahiasDisponibles.isEmpty()) {
                    throw new BusinessException("No hay bahías disponibles");
                }
                reserva.setBahia(bahiasDisponibles.get(0));
            }
            reserva.getBahia().setEstado(EstadoBahia.EN_USO);
            bahiaRepository.save(reserva.getBahia());
        } else if (nuevoEstado == EstadoReserva.COMPLETADA) {
            reserva.setFechaFin(LocalDateTime.now());
            reserva.getBahia().setEstado(EstadoBahia.DISPONIBLE);
            bahiaRepository.save(reserva.getBahia());

            // Actualizar perfil del cliente
            actualizarPerfilCliente(reserva);
        }

        reserva = reservaRepository.save(reserva);
        return convertirADTO(reserva);
    }

    @Transactional
    public ReservaDTO iniciarServicio(Long reservaId, Long bahiaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new BusinessException("Solo se pueden iniciar reservas CONFIRMADAS");
        }

        Bahia bahia = bahiaRepository.findById(bahiaId)
                .orElseThrow(() -> new ResourceNotFoundException("Bahía no encontrada"));

        if (!bahia.getDisponible() || bahia.getEstado() != EstadoBahia.DISPONIBLE) {
            throw new BusinessException("La bahía no está disponible");
        }

        reserva.setBahia(bahia);
        reserva.setEstado(EstadoReserva.EN_PROGRESO);
        reserva.setFechaInicio(LocalDateTime.now());

        bahia.setEstado(EstadoBahia.EN_USO);
        bahia.setDisponible(false);

        bahiaRepository.save(bahia);
        reserva = reservaRepository.save(reserva);

        return convertirADTO(reserva);
    }

    private void actualizarPerfilCliente(Reserva reserva) {
        PerfilCliente perfil = perfilClienteRepository
                .findByUsuarioId(reserva.getCliente().getId())
                .orElse(null);

        if (perfil != null) {
            perfil.setTotalServicios(perfil.getTotalServicios() + 1);
            perfil.setPuntosFidelidad(perfil.getPuntosFidelidad() + 10);
            perfilClienteRepository.save(perfil);
        }
    }

    @Transactional
    public void cancelarReserva(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        reserva.setEstado(EstadoReserva.CANCELADA);
        if (reserva.getBahia() != null) {
            reserva.getBahia().setEstado(EstadoBahia.DISPONIBLE);
            bahiaRepository.save(reserva.getBahia());
        }

        reservaRepository.save(reserva);
    }

    private ReservaDTO convertirADTO(Reserva reserva) {
        ReservaDTO dto = modelMapper.map(reserva, ReservaDTO.class);
        if (reserva.getCliente() != null) {
            dto.setClienteNombre(reserva.getCliente().getNombre());
        }
        if (reserva.getServicio() != null) {
            dto.setServicioNombre(reserva.getServicio().getNombre());
        }
        if (reserva.getBahia() != null) {
            dto.setBahiaNumero(reserva.getBahia().getNumero());
        }
        return dto;
    }
}
