package com.carwash.controller;

import com.carwash.dto.*;
import com.carwash.service.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {
    private final PagoService pagoService;

    @PostMapping
    public ResponseEntity<ApiResponse<PagoDTO>> crear(
            @Valid @RequestBody PagoCreateDTO createDTO) {
        PagoDTO pago = pagoService.crearPago(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Pago procesado exitosamente", pago));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PagoDTO>> obtenerPorId(@PathVariable Long id) {
        PagoDTO pago = pagoService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.success(pago));
    }

    @GetMapping("/reserva/{reservaId}")
    public ResponseEntity<ApiResponse<PagoDTO>> obtenerPorReserva(
            @PathVariable Long reservaId) {
        PagoDTO pago = pagoService.obtenerPorReserva(reservaId);
        return ResponseEntity.ok(ApiResponse.success(pago));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<List<PagoDTO>>> listarTodos() {
        List<PagoDTO> pagos = pagoService.listarTodos();
        return ResponseEntity.ok(ApiResponse.success(pagos));
    }

    @GetMapping("/ingresos/hoy")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<BigDecimal>> obtenerIngresosHoy() {
        BigDecimal ingresos = pagoService.calcularIngresosHoy();
        return ResponseEntity.ok(ApiResponse.success(ingresos));
    }

    @GetMapping("/ingresos/mes")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<BigDecimal>> obtenerIngresosMes() {
        BigDecimal ingresos = pagoService.calcularIngresosMes();
        return ResponseEntity.ok(ApiResponse.success(ingresos));
    }
}
