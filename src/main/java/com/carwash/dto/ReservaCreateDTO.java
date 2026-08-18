package com.carwash.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaCreateDTO {
    @NotNull(message = "El servicio es requerido")
    private Long servicioId;

    @NotNull(message = "La fecha de reserva es requerida")
    @Future(message = "La fecha debe ser futura")
    private LocalDateTime fechaReserva;

    @NotBlank(message = "La placa del vehículo es requerida")
    private String vehiculoPlaca;

    private String vehiculoModelo;
    private String codigoCupon;
    private String observaciones;
}
