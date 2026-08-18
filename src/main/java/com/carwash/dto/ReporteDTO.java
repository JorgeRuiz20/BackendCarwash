package com.carwash.dto;

import com.carwash.model.TipoReporte;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteDTO {
    private Long id;
    private TipoReporte tipo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer totalReservas;
    private BigDecimal ingresoTotal;
    private Double consumoAgua;
    private Double consumoEnergia;
    private LocalDate fechaGeneracion;
}
