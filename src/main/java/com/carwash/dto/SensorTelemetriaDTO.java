package com.carwash.dto;

import com.carwash.model.TipoSensor;
import com.carwash.model.NivelAlerta;
import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorTelemetriaDTO {
    private Long id;

    @NotNull(message = "La bahía es requerida")
    private Long bahiaId;

    @NotNull(message = "El tipo de sensor es requerido")
    private TipoSensor tipoSensor;

    @NotNull(message = "El valor es requerido")
    private Double valor;

    private String unidad;
    private LocalDateTime fechaLectura;
    private Boolean alertaActiva;
    private String mensajeAlerta;

    // NUEVOS CAMPOS PARA SISTEMA DE ALERTAS
    private NivelAlerta nivelAlerta;
    private Boolean alertaResuelta;
    private LocalDateTime fechaResolucion;
    private String operadorResuelveName; // Solo nombre
    private String comentarioResolucion;
}
