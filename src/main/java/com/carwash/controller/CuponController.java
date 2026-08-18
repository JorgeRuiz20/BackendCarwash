package com.carwash.controller;

import com.carwash.dto.*;
import com.carwash.service.CuponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cupones")
@RequiredArgsConstructor
public class CuponController {
    private final CuponService cuponService;

    /**
     * ✅ NUEVO ENDPOINT - Lista TODOS los cupones (activos e inactivos)
     * Útil para el panel de administración
     */
    @GetMapping("/todos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<List<CuponDTO>>> listarTodos() {
        List<CuponDTO> cupones = cuponService.listarTodos();
        return ResponseEntity.ok(ApiResponse.success(cupones));
    }

    /**
     * Lista solo cupones activos
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<List<CuponDTO>>> listarActivos() {
        List<CuponDTO> cupones = cuponService.listarActivos();
        return ResponseEntity.ok(ApiResponse.success(cupones));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<CuponDTO>> obtenerPorId(@PathVariable Long id) {
        CuponDTO cupon = cuponService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.success(cupon));
    }

    /**
     * ✅ Validar un cupón por código
     */
    @PostMapping("/validar")
    public ResponseEntity<ApiResponse<CuponDTO>> validarCupon(@RequestParam String codigo) {
        CuponDTO cupon = cuponService.validarCupon(codigo);
        return ResponseEntity.ok(ApiResponse.success("Cupón válido", cupon));
    }

    /**
     * ✅ Crear un nuevo cupón promocional
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<CuponDTO>> crear(
            @Valid @RequestBody CuponDTO cuponDTO) {
        CuponDTO creado = cuponService.crearCupon(cuponDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Cupón creado exitosamente", creado));
    }

    /**
     * ✅ Desactivar un cupón
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<Void>> desactivar(@PathVariable Long id) {
        cuponService.desactivarCupon(id);
        return ResponseEntity.ok(ApiResponse.success("Cupón desactivado exitosamente", null));
    }
}