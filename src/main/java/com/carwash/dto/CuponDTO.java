package com.carwash.dto;

import com.carwash.model.TipoDescuento;
import lombok.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuponDTO {
    private Long id;

    @NotBlank(message = "El código es requerido")
    private String codigo;

    private String descripcion;

    @NotNull(message = "El tipo de descuento es requerido")
    private TipoDescuento tipoDescuento;

    @NotNull(message = "El valor del descuento es requerido")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal valorDescuento;

    @NotNull(message = "La fecha de inicio es requerida")
    private LocalDateTime fechaInicio;

    @NotNull(message = "La fecha de expiración es requerida")
    private LocalDateTime fechaExpiracion;

    private Integer usosMaximos;
    private Integer usosActuales;
    private Boolean activo;
}
