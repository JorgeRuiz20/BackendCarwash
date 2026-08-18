package com.carwash.config;

import com.carwash.model.*;
import com.carwash.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final UsuarioRepository usuarioRepository;
    private final PerfilClienteRepository perfilClienteRepository;
    private final ServicioRepository servicioRepository;
    private final BahiaRepository bahiaRepository;
    private final TarjetaCreditoRepository tarjetaCreditoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("🚀 Iniciando DataInitializer...");
        
        // Inicializar datos base si la BD está vacía
        if (usuarioRepository.count() == 0) {
            log.info("📊 Base de datos vacía, inicializando datos...");
            initializeData();
        }
        
        // ✅ MEJORA: Siempre reparar perfiles faltantes (importante para desarrollo)
        log.info("🔍 Verificando perfiles de clientes...");
        crearPerfilesFaltantes();
        
        log.info("✅ DataInitializer completado");
    }

    /**
     * ✅ CORRECCIÓN: Método mejorado para crear perfiles faltantes
     * Ahora se ejecuta SIEMPRE, no solo cuando la BD está vacía
     */
    private void crearPerfilesFaltantes() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        int perfilesCreados = 0;
        
        for (Usuario usuario : usuarios) {
            // ✅ Solo crear perfiles para usuarios tipo CLIENTE
            if (usuario.getTipo() == TipoUsuario.CLIENTE) {
                // Si el usuario no tiene perfil, lo creamos automáticamente
                if (perfilClienteRepository.findByUsuario(usuario).isEmpty()) {
                    PerfilCliente perfil = PerfilCliente.builder()
                            .usuario(usuario)
                            .puntosFidelidad(0)
                            .totalServicios(0)
                            .totalGastado(BigDecimal.ZERO)
                            .build();
                    perfilClienteRepository.save(perfil);
                    perfilesCreados++;
                    log.info("✅ Perfil de cliente creado automáticamente para: {} (ID: {})", 
                            usuario.getEmail(), usuario.getId());
                }
            }
        }
        
        if (perfilesCreados > 0) {
            log.info("🎉 Se crearon {} perfiles de cliente", perfilesCreados);
        } else {
            log.info("✓ Todos los clientes ya tienen perfil");
        }
    }

    private void initializeData() {
        log.info("📝 Creando usuarios de prueba...");
        
        // Crear usuario administrador
        Usuario admin = Usuario.builder()
                .email("admin@carwash.com")
                .password(passwordEncoder.encode("admin123"))
                .nombre("Administrador")
                .apellido("Sistema")
                .telefono("999999999")
                .tipo(TipoUsuario.ADMINISTRADOR)
                .activo(true)
                .fechaRegistro(LocalDateTime.now())
                .build();
        usuarioRepository.save(admin);
        log.info("✓ Admin creado: {}", admin.getEmail());

        // Crear usuario operador
        Usuario operador = Usuario.builder()
                .email("operador@carwash.com")
                .password(passwordEncoder.encode("operador123"))
                .nombre("Carlos")
                .apellido("López")
                .telefono("988888888")
                .tipo(TipoUsuario.OPERADOR)
                .activo(true)
                .fechaRegistro(LocalDateTime.now())
                .build();
        usuarioRepository.save(operador);
        log.info("✓ Operador creado: {}", operador.getEmail());

        // Crear usuario cliente de prueba
        Usuario cliente = Usuario.builder()
                .email("cliente@test.com")
                .password(passwordEncoder.encode("cliente123"))
                .nombre("Juan")
                .apellido("Pérez")
                .telefono("977777777")
                .tipo(TipoUsuario.CLIENTE)
                .activo(true)
                .fechaRegistro(LocalDateTime.now())
                .build();
        cliente = usuarioRepository.save(cliente);
        log.info("✓ Cliente creado: {}", cliente.getEmail());

        // ✅ IMPORTANTE: Crear perfil para el cliente
        PerfilCliente perfil = PerfilCliente.builder()
                .usuario(cliente)
                .puntosFidelidad(0)
                .totalServicios(0)
                .totalGastado(BigDecimal.ZERO)
                .build();
        perfilClienteRepository.save(perfil);
        log.info("✓ Perfil de fidelización creado para: {}", cliente.getEmail());

        // Crear más clientes de prueba
        crearClientesAdicionales();
        
        // Crear servicios
        log.info("🛠️ Creando servicios...");
        crearServicios();
        
        // Crear bahías
        log.info("🚗 Creando bahías...");
        crearBahias();
        
        // Crear tarjetas de crédito de prueba
        log.info("💳 Creando tarjetas de prueba...");
        crearTarjetas();
    }

    /**
     * ✅ NUEVO: Crear clientes adicionales para pruebas
     */
    private void crearClientesAdicionales() {
        String[][] clientesData = {
            {"maria@test.com", "maria123", "María", "García"},
            {"pedro@test.com", "pedro123", "Pedro", "Rodríguez"},
            {"ana@test.com", "ana123", "Ana", "Martínez"}
        };
        
        for (String[] data : clientesData) {
            Usuario cliente = Usuario.builder()
                    .email(data[0])
                    .password(passwordEncoder.encode(data[1]))
                    .nombre(data[2])
                    .apellido(data[3])
                    .telefono("977777777")
                    .tipo(TipoUsuario.CLIENTE)
                    .activo(true)
                    .fechaRegistro(LocalDateTime.now())
                    .build();
            cliente = usuarioRepository.save(cliente);
            
            // ✅ Crear perfil automáticamente
            PerfilCliente perfil = PerfilCliente.builder()
                    .usuario(cliente)
                    .puntosFidelidad(0)
                    .totalServicios(0)
                    .totalGastado(BigDecimal.ZERO)
                    .build();
            perfilClienteRepository.save(perfil);
            
            log.info("✓ Cliente creado con perfil: {}", cliente.getEmail());
        }
    }

    private void crearServicios() {
        Servicio[] servicios = {
            Servicio.builder()
                    .nombre("Lavado Básico")
                    .descripcion("Lavado exterior completo del vehículo")
                    .precio(new BigDecimal("25.00"))
                    .duracionMinutos(20)
                    .tipo(TipoServicio.BASICO)
                    .activo(true)
                    .build(),
            Servicio.builder()
                    .nombre("Lavado Completo")
                    .descripcion("Lavado exterior e interior, aspirado y limpieza de tapices")
                    .precio(new BigDecimal("45.00"))
                    .duracionMinutos(40)
                    .tipo(TipoServicio.COMPLETO)
                    .activo(true)
                    .build(),
            Servicio.builder()
                    .nombre("Lavado Premium")
                    .descripcion("Lavado completo + encerado + pulido + aromatización")
                    .precio(new BigDecimal("75.00"))
                    .duracionMinutos(60)
                    .tipo(TipoServicio.PREMIUM)
                    .activo(true)
                    .build(),
            Servicio.builder()
                    .nombre("Lavado Express")
                    .descripcion("Lavado rápido exterior")
                    .precio(new BigDecimal("15.00"))
                    .duracionMinutos(10)
                    .tipo(TipoServicio.EXPRESS)
                    .activo(true)
                    .build()
        };

        for (Servicio servicio : servicios) {
            servicioRepository.save(servicio);
            log.info("✓ Servicio creado: {} - S/{}", servicio.getNombre(), servicio.getPrecio());
        }
    }

    private void crearBahias() {
        for (int i = 1; i <= 4; i++) {
            Bahia bahia = Bahia.builder()
                    .numero("B" + i)
                    .nombre("Bahía " + i)
                    .estado(EstadoBahia.DISPONIBLE)
                    .disponible(true)
                    .build();
            bahiaRepository.save(bahia);
            log.info("✓ Bahía creada: {}", bahia.getNombre());
        }
    }

    private void crearTarjetas() {
        TarjetaCredito[] tarjetas = {
            TarjetaCredito.builder()
                    .numeroTarjeta("4532015112830366")
                    .cvv("123")
                    .fechaExpiracion("12/25")
                    .nombreTitular("Juan Pérez")
                    .activa(true)
                    .build(),
            TarjetaCredito.builder()
                    .numeroTarjeta("5425233430109903")
                    .cvv("456")
                    .fechaExpiracion("06/26")
                    .nombreTitular("María García")
                    .activa(true)
                    .build(),
            TarjetaCredito.builder()
                    .numeroTarjeta("378282246310005")
                    .cvv("789")
                    .fechaExpiracion("09/27")
                    .nombreTitular("Pedro Rodríguez")
                    .activa(true)
                    .build()
        };

        for (TarjetaCredito tarjeta : tarjetas) {
            tarjetaCreditoRepository.save(tarjeta);
            log.info("✓ Tarjeta creada: **** **** **** {}", 
                    tarjeta.getNumeroTarjeta().substring(12));
        }
    }
}