package com.carwash.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FidelizacionDTO {
    private Integer puntosActuales;
    private Integer totalServicios;
    private String totalGastado;
    private Integer cuponesActivos;
    private Integer cuponesUsados;
    
    // Información de cupones disponibles para canjear
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CuponDisponible {
        private Integer porcentaje;
        private Integer puntosRequeridos;
        private Boolean puedeCanjearlo;
        private String descripcion;
    }
    
    private java.util.List<CuponDisponible> cuponesDisponibles;
}
