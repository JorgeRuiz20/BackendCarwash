package com.carwash.dto;

import com.carwash.model.EstadoBahia;
import lombok.*;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BahiaDTO {
    private Long id;

    @NotBlank(message = "El número de bahía es requerido")
    private String numero;

    private String nombre;
    private EstadoBahia estado;
    private Boolean disponible;
}
