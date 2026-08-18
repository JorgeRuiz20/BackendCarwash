package com.carwash.controller;

import com.carwash.dto.*;
import com.carwash.service.CuponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para gestión de fidelización y cupones de puntos
 * Este controlador NO interfiere con el CuponController existente (para cupones promocionales)
 */
@RestController
@RequestMapping("/api/fidelizacion")
@RequiredArgsConstructor
@Tag(name = "Fidelización", description = "Gestión de puntos de fidelización y cupones")
@SecurityRequirement(name = "bearer-jwt")
public class FidelizacionController {
    
    private final CuponService cuponService;
    
    @PostMapping("/canjear")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Canjear puntos por un cupón", 
               description = "Canjea puntos de fidelidad por un cupón de descuento. " +
                           "Opciones: 100 puntos = 5%, 200 puntos = 10%, 500 puntos = 20%")
    public ResponseEntity<ApiResponse<CuponFidelizacionDTO>> canjearPuntos(
            @Valid @RequestBody CanjearCuponRequest request,
            Authentication authentication) {
        
        String email = authentication.getName();
        CuponFidelizacionDTO cupon = cuponService.canjearPuntosPorCupon(email, request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.<CuponFidelizacionDTO>builder()
                        .success(true)
                        .message("¡Cupón canjeado exitosamente! 🎉")
                        .data(cupon)
                        .build());
    }
    
    @GetMapping("/mis-cupones")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Obtener mis cupones de fidelización", 
               description = "Obtiene el historial completo de cupones canjeados con puntos")
    public ResponseEntity<ApiResponse<List<CuponFidelizacionDTO>>> obtenerMisCupones(Authentication authentication) {
        String email = authentication.getName();
        List<CuponFidelizacionDTO> cupones = cuponService.obtenerCuponesFidelizacion(email);
        
        return ResponseEntity.ok(ApiResponse.<List<CuponFidelizacionDTO>>builder()
                .success(true)
                .message("Cupones obtenidos exitosamente")
                .data(cupones)
                .build());
    }
    
    @GetMapping("/cupones-activos")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Obtener cupones activos", 
               description = "Obtiene solo los cupones de fidelización activos y válidos")
    public ResponseEntity<ApiResponse<List<CuponFidelizacionDTO>>> obtenerCuponesActivos(Authentication authentication) {
        String email = authentication.getName();
        List<CuponFidelizacionDTO> cupones = cuponService.obtenerCuponesFidelizacionActivos(email);
        
        return ResponseEntity.ok(ApiResponse.<List<CuponFidelizacionDTO>>builder()
                .success(true)
                .message("Cupones activos obtenidos exitosamente")
                .data(cupones)
                .build());
    }
    
    @GetMapping("/validar-cupon/{codigoCupon}")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Validar un cupón de fidelización", 
               description = "Verifica si un cupón de fidelización es válido para usar")
    public ResponseEntity<ApiResponse<CuponFidelizacionDTO>> validarCupon(
            @PathVariable String codigoCupon,
            Authentication authentication) {
        
        String email = authentication.getName();
        CuponFidelizacionDTO cupon = cuponService.validarCuponFidelizacion(codigoCupon, email);
        
        return ResponseEntity.ok(ApiResponse.<CuponFidelizacionDTO>builder()
                .success(true)
                .message("✅ Cupón válido")
                .data(cupon)
                .build());
    }
    
    @GetMapping("/info")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Obtener información de fidelización", 
               description = "Obtiene información completa del programa de fidelización: " +
                           "puntos actuales, cupones disponibles, historial, etc.")
    public ResponseEntity<ApiResponse<FidelizacionDTO>> obtenerInfoFidelizacion(Authentication authentication) {
        String email = authentication.getName();
        FidelizacionDTO info = cuponService.obtenerInfoFidelizacion(email);
        
        return ResponseEntity.ok(ApiResponse.<FidelizacionDTO>builder()
                .success(true)
                .message("Información de fidelización obtenida exitosamente")
                .data(info)
                .build());
    }
}