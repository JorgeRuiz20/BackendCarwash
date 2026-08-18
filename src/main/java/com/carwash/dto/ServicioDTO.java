package com.carwash.dto;

import com.carwash.model.TipoServicio;
import lombok.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicioDTO {
    private Long id;

    @NotBlank(message = "El nombre es requerido")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El precio es requerido")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    private BigDecimal precio;

    @NotNull(message = "La duración es requerida")
    @Min(value = 1, message = "La duración debe ser al menos 1 minuto")
    private Integer duracionMinutos;

    private TipoServicio tipo;
    private Boolean activo;
}
