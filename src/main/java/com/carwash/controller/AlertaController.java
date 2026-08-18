package com.carwash.controller;

import com.carwash.dto.*;
import com.carwash.service.AlertaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('OPERADOR', 'ADMINISTRADOR')")
public class AlertaController {
    private final AlertaService alertaService;

    @GetMapping("/activas")
    public ResponseEntity<ApiResponse<List<SensorTelemetriaDTO>>> listarAlertasActivas() {
        List<SensorTelemetriaDTO> alertas = alertaService.obtenerAlertasActivas();
        return ResponseEntity.ok(ApiResponse.success(alertas));
    }

    @GetMapping("/criticas")
    public ResponseEntity<ApiResponse<List<SensorTelemetriaDTO>>> listarAlertasCriticas() {
        List<SensorTelemetriaDTO> alertas = alertaService.obtenerAlertasCriticas();
        return ResponseEntity.ok(ApiResponse.success(alertas));
    }

@PostMapping("/{alertaId}/resolver")
public ResponseEntity<ApiResponse<SensorTelemetriaDTO>> resolverAlerta(
        @PathVariable Long alertaId,
        @RequestParam String comentario,
        @RequestParam(defaultValue = "false") boolean reanudarServicio,
        Authentication auth) {

    String email = auth.getName();

    SensorTelemetriaDTO alerta = alertaService.resolverAlerta(
            alertaId, email, comentario, reanudarServicio); // ✅ 4 parámetros

    return ResponseEntity.ok(ApiResponse.success(
            reanudarServicio ? "Alerta resuelta y servicio reanudado" : "Alerta resuelta",
            alerta));
}}
