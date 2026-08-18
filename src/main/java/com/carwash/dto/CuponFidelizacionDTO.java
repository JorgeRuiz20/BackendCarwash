package com.carwash.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// DTO para cupones de fidelización
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuponFidelizacionDTO {
    private Long id;
    private String codigo;
    private String descripcion;
    private Integer porcentajeDescuento;
    private Integer puntosUsados;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaExpiracion;
    private Boolean usado;
    private Boolean esValido;
    private String estado; // ACTIVO, USADO, EXPIRADO
}