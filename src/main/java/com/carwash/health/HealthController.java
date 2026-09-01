package com.carwash.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@Tag(name = "Health", description = "Endpoint de verificación de estado para Render y UptimeRobot")
public class HealthController {

    @Operation(summary = "Verificar estado del servicio", description = "Retorna OK para mantener activo el backend en Render mediante UptimeRobot")
    @GetMapping(value = { "/health", "/api/health", "/" })
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Carwash Backend is running");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Health check simple en texto plano")
    @GetMapping(value = { "/health/ping", "/api/health/ping" })
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("OK");
    }
}
