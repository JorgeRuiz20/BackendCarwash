package com.carwash.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "reportes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reporte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    private TipoReporte tipo;
    
    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;
    
    @Column(name = "fecha_fin")
    private LocalDate fechaFin;
    
    private Integer totalReservas;
    private BigDecimal ingresoTotal;
    private Double consumoAgua;
    private Double consumoEnergia;
    
    @Column(name = "fecha_generacion")
    private LocalDate fechaGeneracion;
    
    @Column(length = 5000)
    private String datosJson;
}