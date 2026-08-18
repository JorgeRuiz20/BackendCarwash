package com.carwash.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensores_telemetria")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorTelemetria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bahia_id")
    private Bahia bahia;

    @Enumerated(EnumType.STRING)
    private TipoSensor tipoSensor;

    private Double valor;
    private String unidad;

    @Column(name = "fecha_lectura")
    private LocalDateTime fechaLectura;

    @Builder.Default
    private Boolean alertaActiva = false;
    private String mensajeAlerta;

    // NUEVOS CAMPOS PARA SISTEMA DE ALERTAS
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private NivelAlerta nivelAlerta = NivelAlerta.NORMAL;

    @Builder.Default
    private Boolean alertaResuelta = false;
    private LocalDateTime fechaResolucion;

    @ManyToOne
    @JoinColumn(name = "operador_resuelve_id")
    private Usuario operadorResuelve;

    private String comentarioResolucion;
}