package com.carwash.controller;

import com.carwash.dto.*;
import com.carwash.model.EstadoBahia;
import com.carwash.service.BahiaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/bahias")
@RequiredArgsConstructor
public class BahiaController {
    private final BahiaService bahiaService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BahiaDTO>>> listarTodas() {
        List<BahiaDTO> bahias = bahiaService.listarTodas();
        return ResponseEntity.ok(ApiResponse.success(bahias));
    }

    @GetMapping("/disponibles")
    public ResponseEntity<ApiResponse<List<BahiaDTO>>> listarDisponibles() {
        List<BahiaDTO> bahias = bahiaService.listarDisponibles();
        return ResponseEntity.ok(ApiResponse.success(bahias));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BahiaDTO>> obtenerPorId(@PathVariable Long id) {
        BahiaDTO bahia = bahiaService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.success(bahia));
    }

    @GetMapping("/count/disponibles")
    public ResponseEntity<ApiResponse<Long>> contarDisponibles() {
        Long count = bahiaService.contarDisponibles();
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<BahiaDTO>> crear(
            @Valid @RequestBody BahiaDTO bahiaDTO) {
        BahiaDTO creada = bahiaService.crearBahia(bahiaDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bahía creada", creada));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<BahiaDTO>> actualizarEstado(
            @PathVariable Long id,
            @RequestParam EstadoBahia estado) {
        BahiaDTO actualizada = bahiaService.actualizarEstado(id, estado);
        return ResponseEntity.ok(ApiResponse.success("Estado actualizado", actualizada));
    }
}
