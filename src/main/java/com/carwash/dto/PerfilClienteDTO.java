package com.carwash.dto;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilClienteDTO {
    private Long id;

    @NotNull(message = "El ID del usuario es requerido")
    private Long usuarioId;

    @NotNull(message = "Los puntos de fidelidad no pueden ser nulos")
    private Integer puntosFidelidad;

    @NotNull(message = "El total de servicios no puede ser nulo")
    private Integer totalServicios;

    private String vehiculoFavorito;
    private String preferenciaLavado;
}
