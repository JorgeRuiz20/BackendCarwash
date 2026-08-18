package com.carwash.controller;

import com.carwash.dto.*;
import com.carwash.service.SensorTelemetriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/telemetria")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('OPERADOR', 'ADMINISTRADOR')")
public class SensorTelemetriaController {
    private final SensorTelemetriaService sensorService;

    @PostMapping
    public ResponseEntity<ApiResponse<SensorTelemetriaDTO>> registrarLectura(
            @Valid @RequestBody SensorTelemetriaDTO sensorDTO) {
        SensorTelemetriaDTO registrado = sensorService.registrarLectura(sensorDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Lectura registrada", registrado));
    }

    @GetMapping("/bahia/{bahiaId}")
    public ResponseEntity<ApiResponse<List<SensorTelemetriaDTO>>> obtenerLecturasPorBahia(
            @PathVariable Long bahiaId) {
        List<SensorTelemetriaDTO> lecturas = sensorService.obtenerLecturasPorBahia(bahiaId);
        return ResponseEntity.ok(ApiResponse.success(lecturas));
    }

    @GetMapping("/alertas")
    public ResponseEntity<ApiResponse<List<SensorTelemetriaDTO>>> obtenerAlertasActivas() {
        List<SensorTelemetriaDTO> alertas = sensorService.obtenerAlertasActivas();
        return ResponseEntity.ok(ApiResponse.success(alertas));
    }

    @GetMapping("/consumo-agua/promedio")
    public ResponseEntity<ApiResponse<Double>> obtenerPromedioConsumoAgua(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        Double promedio = sensorService.obtenerPromedioConsumoAgua(inicio, fin);
        return ResponseEntity.ok(ApiResponse.success(promedio));
    }
}
