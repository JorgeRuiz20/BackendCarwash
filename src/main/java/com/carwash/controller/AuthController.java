package com.carwash.controller;

import com.carwash.dto.*;
import com.carwash.service.UsuarioService;
import com.carwash.security.JwtService;
import com.carwash.security.RateLimitingFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    private final RateLimitingFilter rateLimitingFilter;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UsuarioDTO>> registrar(
            @Valid @RequestBody UsuarioRegistroDTO registroDTO) {
        UsuarioDTO usuario = usuarioService.registrarUsuario(registroDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Usuario registrado exitosamente", usuario));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(
            @Valid @RequestBody LoginDTO loginDTO,
            HttpServletRequest request) {
        
        String clientIp = getClientIP(request);
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getEmail(),
                            loginDTO.getPassword()));

            String token = jwtService.generateToken(authentication);
            UsuarioDTO usuario = usuarioService.obtenerPorEmail(loginDTO.getEmail());

            // Registrar intento exitoso
            rateLimitingFilter.recordLoginAttempt(clientIp, true);

            AuthResponseDTO response = AuthResponseDTO.builder()
                    .token(token)
                    .usuario(usuario)
                    .build();

            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (AuthenticationException e) {
            // Registrar intento fallido
            rateLimitingFilter.recordLoginAttempt(clientIp, false);
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Credenciales inválidas"));
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateToken() {
        // Si llega aquí, el token es válido (filtro JWT lo valida)
        return ResponseEntity.ok(ApiResponse.success(true));
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}