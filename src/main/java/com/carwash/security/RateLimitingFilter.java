package com.carwash.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    
    // Almacenamiento de intentos por IP
    private final Map<String, LoginAttempt> loginAttempts = new ConcurrentHashMap<>();
    
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MINUTES = 15;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        
        // Solo aplicar rate limiting a endpoints de autenticación
        if (requestPath.equals("/api/auth/login") && request.getMethod().equals("POST")) {
            String clientIp = getClientIP(request);
            
            if (isBlocked(clientIp)) {
                sendBlockedResponse(response, clientIp);
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    private boolean isBlocked(String ip) {
        LoginAttempt attempt = loginAttempts.get(ip);
        if (attempt == null) {
            return false;
        }

        // Verificar si el bloqueo ha expirado
        if (attempt.isBlocked() && attempt.isLockoutExpired()) {
            loginAttempts.remove(ip);
            return false;
        }

        return attempt.isBlocked();
    }

    public void recordLoginAttempt(String ip, boolean success) {
        LoginAttempt attempt = loginAttempts.computeIfAbsent(ip, k -> new LoginAttempt());
        
        if (success) {
            loginAttempts.remove(ip);
        } else {
            attempt.incrementAttempts();
            if (attempt.getAttempts() >= MAX_ATTEMPTS) {
                attempt.block();
            }
        }
    }

    private void sendBlockedResponse(HttpServletResponse response, String ip) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", "Demasiados intentos de inicio de sesión. Cuenta bloqueada temporalmente.");
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("lockoutMinutes", LOCKOUT_DURATION_MINUTES);

        objectMapper.writeValue(response.getOutputStream(), body);
    }

    // Clase interna para tracking de intentos
    private static class LoginAttempt {
        private int attempts = 0;
        private LocalDateTime blockTime;

        public void incrementAttempts() {
            attempts++;
        }

        public int getAttempts() {
            return attempts;
        }

        public void block() {
            blockTime = LocalDateTime.now();
        }

        public boolean isBlocked() {
            return blockTime != null;
        }

        public boolean isLockoutExpired() {
            if (blockTime == null) return true;
            return LocalDateTime.now().isAfter(blockTime.plusMinutes(LOCKOUT_DURATION_MINUTES));
        }
    }
}