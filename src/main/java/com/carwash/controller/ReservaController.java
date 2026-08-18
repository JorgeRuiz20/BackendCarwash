package com.carwash.controller;

import com.carwash.dto.*;
import com.carwash.model.EstadoReserva;
import com.carwash.service.ReservaService;
import com.carwash.service.UsuarioService; // ✅ NUEVO IMPORT
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * ✅ CORREGIDO: Adaptado para trabajar con JWT basado en EMAIL
 * Ahora obtiene el ID del usuario desde el email del authentication
 */
@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
public class ReservaController {
    private final ReservaService reservaService;
    private final UsuarioService usuarioService; // ✅ NUEVO: Inyectado para obtener usuario por email

    @PostMapping
    public ResponseEntity<ApiResponse<ReservaDTO>> crear(
            @Valid @RequestBody ReservaCreateDTO createDTO,
            Authentication authentication) {
        // ✅ CORREGIDO: Obtener email del usuario autenticado
        String email = authentication.getName();
        
        // ✅ CORREGIDO: Buscar usuario por email y obtener su ID
        UsuarioDTO usuario = usuarioService.obtenerPorEmail(email);
        Long clienteId = usuario.getId();
        
        ReservaDTO reserva = reservaService.crearReserva(clienteId, createDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Reserva creada exitosamente", reserva));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservaDTO>> obtenerPorId(@PathVariable Long id) {
        ReservaDTO reserva = reservaService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.success(reserva));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<ApiResponse<List<ReservaDTO>>> listarPorCliente(
            @PathVariable Long clienteId) {
        List<ReservaDTO> reservas = reservaService.listarPorCliente(clienteId);
        return ResponseEntity.ok(ApiResponse.success(reservas));
    }

    @GetMapping("/mis-reservas")
    public ResponseEntity<ApiResponse<List<ReservaDTO>>> listarMisReservas(
            Authentication authentication) {
        // ✅ CORREGIDO: Obtener ID desde email
        String email = authentication.getName();
        UsuarioDTO usuario = usuarioService.obtenerPorEmail(email);
        Long clienteId = usuario.getId();
        
        List<ReservaDTO> reservas = reservaService.listarPorCliente(clienteId);
        return ResponseEntity.ok(ApiResponse.success(reservas));
    }

    @GetMapping("/hoy")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<List<ReservaDTO>>> listarReservasHoy() {
        List<ReservaDTO> reservas = reservaService.listarReservasHoy();
        return ResponseEntity.ok(ApiResponse.success(reservas));
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<List<ReservaDTO>>> listarPorEstado(
            @PathVariable EstadoReserva estado) {
        List<ReservaDTO> reservas = reservaService.listarPorEstado(estado);
        return ResponseEntity.ok(ApiResponse.success(reservas));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<ReservaDTO>> actualizarEstado(
            @PathVariable Long id,
            @RequestParam EstadoReserva estado) {
        ReservaDTO actualizada = reservaService.actualizarEstado(id, estado);
        return ResponseEntity.ok(ApiResponse.success("Estado actualizado", actualizada));
    }

    @PatchMapping("/{id}/iniciar")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<ReservaDTO>> iniciarServicio(
            @PathVariable Long id,
            @RequestParam Long bahiaId) {
        ReservaDTO actualizada = reservaService.iniciarServicio(id, bahiaId);
        return ResponseEntity.ok(ApiResponse.success("Servicio iniciado", actualizada));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelar(@PathVariable Long id) {
        reservaService.cancelarReserva(id);
        return ResponseEntity.ok(ApiResponse.success("Reserva cancelada", null));
    }
}