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
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CuponService {

    private final CuponRepository cuponRepository;
    private final UsuarioRepository usuarioRepository;
    private final PerfilClienteRepository perfilClienteRepository;
    private final ModelMapper modelMapper;

    // ========== CUPONES PROMOCIONALES ==========

    /**
     * ✅ Lista TODOS los cupones (no solo activos)
     */
    public List<CuponDTO> listarTodos() {
        return cuponRepository.findAll().stream()
                .map(c -> modelMapper.map(c, CuponDTO.class))
                .sorted((a, b) -> b.getId().compareTo(a.getId())) // Más recientes primero
                .toList();
    }

    /**
     * Lista solo cupones activos
     */
    public List<CuponDTO> listarActivos() {
        return cuponRepository.findByActivoTrue().stream()
                .map(c -> modelMapper.map(c, CuponDTO.class))
                .toList();
    }

    public CuponDTO obtenerPorId(Long id) {
        Cupon cupon = cuponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cupón no encontrado"));
        return modelMapper.map(cupon, CuponDTO.class);
    }

    /**
     * ✅ VALIDACIÓN MEJORADA con mensajes específicos
     */
    public CuponDTO validarCupon(String codigo) {
        log.info("Validando cupón: {}", codigo);
        
        Cupon cupon = cuponRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Cupón no encontrado con código: " + codigo));

        // ✅ VALIDACIONES ESPECÍFICAS
        if (!cupon.getActivo()) {
            log.warn("Cupón {} está desactivado", codigo);
            throw new BusinessException("Este cupón está desactivado");
        }
        
        LocalDateTime ahora = LocalDateTime.now();
        
        if (ahora.isBefore(cupon.getFechaInicio())) {
            String fechaInicio = cupon.getFechaInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            log.warn("Cupón {} aún no está disponible. Inicia: {}", codigo, fechaInicio);
            throw new BusinessException("Este cupón estará disponible a partir del " + fechaInicio);
        }
        
        if (ahora.isAfter(cupon.getFechaExpiracion())) {
            String fechaExp = cupon.getFechaExpiracion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            log.warn("Cupón {} ha expirado. Fecha: {}", codigo, fechaExp);
            throw new BusinessException("Este cupón expiró el " + fechaExp);
        }
        
        if (cupon.getUsosMaximos() != null && cupon.getUsosActuales() >= cupon.getUsosMaximos()) {
            log.warn("Cupón {} ha alcanzado su límite de usos: {}/{}", 
                codigo, cupon.getUsosActuales(), cupon.getUsosMaximos());
            throw new BusinessException("Este cupón ya alcanzó su límite de usos");
        }

        log.info("✅ Cupón {} validado correctamente", codigo);
        return modelMapper.map(cupon, CuponDTO.class);
    }

    @Transactional
    public CuponDTO crearCupon(CuponDTO cuponDTO) {
        log.info("Creando cupón con código: {}", cuponDTO.getCodigo());
        
        if (cuponRepository.existsByCodigo(cuponDTO.getCodigo())) {
            throw new BusinessException("Ya existe un cupón con el código: " + cuponDTO.getCodigo());
        }

        Cupon cupon = modelMapper.map(cuponDTO, Cupon.class);
        
        // ✅ Asegurar valores por defecto
        if (cupon.getActivo() == null) {
            cupon.setActivo(true);
        }
        if (cupon.getUsosActuales() == null) {
            cupon.setUsosActuales(0);
        }
        if (cupon.getTipoOrigen() == null) {
            cupon.setTipoOrigen(TipoOrigenCupon.PROMOCIONAL);
        }
        
        cupon = cuponRepository.save(cupon);
        log.info("✅ Cupón creado exitosamente: {}", cupon.getCodigo());
        
        return modelMapper.map(cupon, CuponDTO.class);
    }

    @Transactional
    public void desactivarCupon(Long id) {
        Cupon cupon = cuponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cupón no encontrado"));
        cupon.setActivo(false);
        cuponRepository.save(cupon);
        log.info("Cupón {} desactivado", cupon.getCodigo());
    }

    // ========== CUPONES DE FIDELIZACIÓN ==========

    @Transactional
    public CuponFidelizacionDTO canjearPuntosPorCupon(String email, CanjearCuponRequest request) {
        log.info("Iniciando canje de cupón para usuario: {}", email);

        if (!TipoCuponFidelizacion.esPuntosValido(request.getPuntosACanjear())) {
            throw new BusinessException(
                    "Puntos inválidos. Solo puedes canjear: 100 puntos (5%), 200 puntos (10%), o 500 puntos (20%)");
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        PerfilCliente perfil = perfilClienteRepository.findByUsuario(usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de cliente no encontrado"));

        if (!perfil.tienePuntosSuficientes(request.getPuntosACanjear())) {
            throw new BusinessException(
                    String.format("Puntos insuficientes. Tienes %d puntos, necesitas %d puntos",
                            perfil.getPuntosFidelidad(), request.getPuntosACanjear()));
        }

        TipoCuponFidelizacion tipoCupon = TipoCuponFidelizacion.getPorPuntos(request.getPuntosACanjear());
        String codigoGenerado = generarCodigoCupon(tipoCupon.getPorcentaje());

        Cupon cupon = Cupon.builder()
                .codigo(codigoGenerado)
                .descripcion(tipoCupon.getDescripcion())
                .tipoDescuento(TipoDescuento.PORCENTAJE)
                .valorDescuento(BigDecimal.valueOf(tipoCupon.getPorcentaje()))
                .fechaInicio(LocalDateTime.now())
                .fechaExpiracion(LocalDateTime.now().plusDays(30))
                .usosMaximos(1)
                .usosActuales(0)
                .activo(true)
                .perfilCliente(perfil)
                .puntosUsados(tipoCupon.getPuntosRequeridos())
                .tipoOrigen(TipoOrigenCupon.FIDELIZACION)
                .build();

        cupon = cuponRepository.save(cupon);
        perfil.descontarPuntos(request.getPuntosACanjear());
        perfilClienteRepository.save(perfil);

        log.info("✅ Cupón {} creado exitosamente para usuario {}", cupon.getCodigo(), email);

        return convertirACuponFidelizacionDTO(cupon);
    }

    private String generarCodigoCupon(int porcentaje) {
        String codigo;
        do {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            int random = new Random().nextInt(9999);
            codigo = String.format("FIDE-%dPCT-%s-%04d", porcentaje, timestamp, random);
        } while (cuponRepository.existsByCodigo(codigo));

        return codigo;
    }

    @Transactional(readOnly = true)
    public List<CuponFidelizacionDTO> obtenerCuponesFidelizacion(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        PerfilCliente perfil = perfilClienteRepository.findByUsuario(usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil no encontrado"));

        List<Cupon> cupones = cuponRepository.findCuponesFidelizacionPerfil(perfil);

        return cupones.stream()
                .map(this::convertirACuponFidelizacionDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CuponFidelizacionDTO> obtenerCuponesFidelizacionActivos(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        PerfilCliente perfil = perfilClienteRepository.findByUsuario(usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil no encontrado"));

        List<Cupon> cupones = cuponRepository.findCuponesFidelizacionActivosPerfil(perfil, LocalDateTime.now());

        return cupones.stream()
                .map(this::convertirACuponFidelizacionDTO)
                .toList();
    }

    /**
     * ✅ VALIDACIÓN MEJORADA para cupones de fidelización
     */
    @Transactional(readOnly = true)
    public CuponFidelizacionDTO validarCuponFidelizacion(String codigoCupon, String emailCliente) {
        log.info("Validando cupón de fidelización: {} para cliente: {}", codigoCupon, emailCliente);
        
        Cupon cupon = cuponRepository.findByCodigo(codigoCupon)
                .orElseThrow(() -> new ResourceNotFoundException("Cupón no encontrado"));

        // Si es cupón de fidelización, verificar que pertenezca al cliente
        if (cupon.getTipoOrigen() == TipoOrigenCupon.FIDELIZACION) {
            Usuario usuario = usuarioRepository.findByEmail(emailCliente)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

            PerfilCliente perfil = perfilClienteRepository.findByUsuario(usuario)
                    .orElseThrow(() -> new ResourceNotFoundException("Perfil no encontrado"));

            if (cupon.getPerfilCliente() == null ||
                    !cupon.getPerfilCliente().getId().equals(perfil.getId())) {
                log.warn("Cupón {} no pertenece al cliente {}", codigoCupon, emailCliente);
                throw new BusinessException("Este cupón no te pertenece");
            }
        }

        // ✅ Verificaciones específicas
        if (!cupon.getActivo()) {
            throw new BusinessException("Este cupón está desactivado");
        }
        
        if (cupon.getUsosMaximos() != null && cupon.getUsosActuales() >= cupon.getUsosMaximos()) {
            throw new BusinessException("Este cupón ya ha sido utilizado");
        }
        
        if (LocalDateTime.now().isAfter(cupon.getFechaExpiracion())) {
            String fechaExp = cupon.getFechaExpiracion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            throw new BusinessException("Este cupón expiró el " + fechaExp);
        }

        log.info("✅ Cupón de fidelización {} validado correctamente", codigoCupon);
        return convertirACuponFidelizacionDTO(cupon);
    }

    @Transactional(readOnly = true)
    public FidelizacionDTO obtenerInfoFidelizacion(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        PerfilCliente perfil = perfilClienteRepository.findByUsuario(usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil no encontrado"));

        Long cuponesActivos = cuponRepository.contarCuponesFidelizacionActivosPerfil(perfil, LocalDateTime.now());
        List<Cupon> cuponesUsados = cuponRepository.findCuponesFidelizacionUsadosPerfil(perfil);

        List<FidelizacionDTO.CuponDisponible> cuponesDisponibles = new ArrayList<>();
        for (TipoCuponFidelizacion tipo : TipoCuponFidelizacion.values()) {
            cuponesDisponibles.add(
                    FidelizacionDTO.CuponDisponible.builder()
                            .porcentaje(tipo.getPorcentaje())
                            .puntosRequeridos(tipo.getPuntosRequeridos())
                            .puedeCanjearlo(perfil.getPuntosFidelidad() >= tipo.getPuntosRequeridos())
                            .descripcion(tipo.getDescripcion())
                            .build());
        }

        return FidelizacionDTO.builder()
                .puntosActuales(perfil.getPuntosFidelidad())
                .totalServicios(perfil.getTotalServicios())
                .totalGastado(perfil.getTotalGastado().toString())
                .cuponesActivos(cuponesActivos.intValue())
                .cuponesUsados(cuponesUsados.size())
                .cuponesDisponibles(cuponesDisponibles)
                .build();
    }

    @Transactional
    public void desactivarCuponesExpirados() {
        List<Cupon> cuponesExpirados = cuponRepository.findCuponesExpirados(LocalDateTime.now());

        for (Cupon cupon : cuponesExpirados) {
            cupon.setActivo(false);
        }

        if (!cuponesExpirados.isEmpty()) {
            cuponRepository.saveAll(cuponesExpirados);
            log.info("Se desactivaron {} cupones expirados", cuponesExpirados.size());
        }
    }

    // ========== MÉTODOS AUXILIARES ==========

    private CuponFidelizacionDTO convertirACuponFidelizacionDTO(Cupon cupon) {
        return CuponFidelizacionDTO.builder()
                .id(cupon.getId())
                .codigo(cupon.getCodigo())
                .descripcion(cupon.getDescripcion())
                .porcentajeDescuento(cupon.getValorDescuento().intValue())
                .puntosUsados(cupon.getPuntosUsados())
                .fechaCreacion(cupon.getFechaInicio())
                .fechaExpiracion(cupon.getFechaExpiracion())
                .usado(cupon.getUsosActuales() >= cupon.getUsosMaximos())
                .esValido(cupon.esValido())
                .build();
    }
}