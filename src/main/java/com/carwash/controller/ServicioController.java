package com.carwash.controller;

import com.carwash.dto.*;
import com.carwash.model.TipoServicio;
import com.carwash.service.ServicioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/servicios")
@RequiredArgsConstructor
public class ServicioController {
    private final ServicioService servicioService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ServicioDTO>>> listarTodos() {
        List<ServicioDTO> servicios = servicioService.listarTodos();
        return ResponseEntity.ok(ApiResponse.success(servicios));
    }

    @GetMapping("/activos")
    public ResponseEntity<ApiResponse<List<ServicioDTO>>> listarActivos() {
        List<ServicioDTO> servicios = servicioService.listarActivos();
        return ResponseEntity.ok(ApiResponse.success(servicios));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServicioDTO>> obtenerPorId(@PathVariable Long id) {
        ServicioDTO servicio = servicioService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.success(servicio));
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<ApiResponse<List<ServicioDTO>>> listarPorTipo(
            @PathVariable TipoServicio tipo) {
        List<ServicioDTO> servicios = servicioService.listarPorTipo(tipo);
        return ResponseEntity.ok(ApiResponse.success(servicios));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<ServicioDTO>> crear(
            @Valid @RequestBody ServicioDTO servicioDTO) {
        ServicioDTO creado = servicioService.crearServicio(servicioDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Servicio creado", creado));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<ServicioDTO>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ServicioDTO servicioDTO) {
        ServicioDTO actualizado = servicioService.actualizarServicio(id, servicioDTO);
        return ResponseEntity.ok(ApiResponse.success("Servicio actualizado", actualizado));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        servicioService.eliminarServicio(id);
        return ResponseEntity.ok(ApiResponse.success("Servicio eliminado", null));
    }
}
