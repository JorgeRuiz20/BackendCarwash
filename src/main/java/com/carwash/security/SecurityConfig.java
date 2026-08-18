package com.carwash.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Value("${cors.allowed.origins:http://localhost:5173,http://localhost:3000}")
    private String[] allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Strength aumentada a 12
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Orígenes específicos - NUNCA usar "*" con credentials
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        
        // Headers permitidos - específicos
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "X-Requested-With"
        ));
        
        // Headers expuestos
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type"
        ));
        
        // Credentials permitidas
        configuration.setAllowCredentials(true);
        
        // Tiempo de caché para preflight
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Deshabilitar CSRF (usando JWT)
            .csrf(csrf -> csrf.disable())
            
            // Configurar CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Manejo de excepciones
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )
            
            // Gestión de sesiones - STATELESS
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Configuración de autorización
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas - SOLO autenticación
                .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                
                // Swagger/OpenAPI - Solo en desarrollo
                .requestMatchers(
                    "/swagger-ui/**", 
                    "/v3/api-docs/**",
                    "/swagger-ui.html"
                ).permitAll()
                
                // Rutas de servicios - Solo lectura pública
                .requestMatchers(HttpMethod.GET, "/api/servicios/activos").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/servicios/{id}").permitAll()
                
                // Bahías - Solo GET público para disponibilidad
                .requestMatchers(HttpMethod.GET, "/api/bahias/disponibles").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/bahias/count/disponibles").permitAll()
                
                // CUPONES - Validación pública
                .requestMatchers(HttpMethod.POST, "/api/cupones/validar").permitAll()
                
                // OPTIONS para CORS
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // ===== RUTAS PROTEGIDAS POR ROL =====
                
                // ADMINISTRADOR - Gestión completa
                .requestMatchers("/api/usuarios/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.POST, "/api/servicios/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.PUT, "/api/servicios/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.DELETE, "/api/servicios/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.POST, "/api/bahias/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/api/cupones/**").hasRole("ADMINISTRADOR")
                
                // OPERADOR y ADMINISTRADOR - Gestión operativa
                .requestMatchers("/api/dashboard/**").hasAnyRole("OPERADOR", "ADMINISTRADOR")
                .requestMatchers("/api/alertas/**").hasAnyRole("OPERADOR", "ADMINISTRADOR")
                .requestMatchers("/api/telemetria/**").hasAnyRole("OPERADOR", "ADMINISTRADOR")
                .requestMatchers("/api/reportes/**").hasAnyRole("OPERADOR", "ADMINISTRADOR")
                .requestMatchers(HttpMethod.GET, "/api/reservas/hoy").hasAnyRole("OPERADOR", "ADMINISTRADOR")
                .requestMatchers(HttpMethod.GET, "/api/reservas/estado/**").hasAnyRole("OPERADOR", "ADMINISTRADOR")
                .requestMatchers(HttpMethod.PATCH, "/api/reservas/*/estado").hasAnyRole("OPERADOR", "ADMINISTRADOR")
                .requestMatchers(HttpMethod.PATCH, "/api/reservas/*/iniciar").hasAnyRole("OPERADOR", "ADMINISTRADOR")
                .requestMatchers(HttpMethod.PATCH, "/api/bahias/*/estado").hasAnyRole("OPERADOR", "ADMINISTRADOR")
                .requestMatchers(HttpMethod.GET, "/api/pagos/ingresos/**").hasAnyRole("OPERADOR", "ADMINISTRADOR")
                
                // CLIENTE - Sus propias reservas y pagos
                .requestMatchers(HttpMethod.POST, "/api/reservas").hasAnyRole("CLIENTE", "ADMINISTRADOR")
                .requestMatchers(HttpMethod.GET, "/api/reservas/mis-reservas").hasAnyRole("CLIENTE", "ADMINISTRADOR")
                .requestMatchers(HttpMethod.POST, "/api/pagos").hasAnyRole("CLIENTE", "ADMINISTRADOR")
                .requestMatchers("/api/fidelizacion/**").hasRole("CLIENTE")
                
                // Cualquier ruta autenticada requiere autenticación
                .anyRequest().authenticated()
            )
            
            // Proveedor de autenticación
            .authenticationProvider(authenticationProvider())
            
            // Filtro JWT
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // Headers de seguridad
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny())
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'")
                )
                .xssProtection(xss -> xss.disable()) // Spring Security 6+ recomienda deshabilitar
            );

        return http.build();
    }
}