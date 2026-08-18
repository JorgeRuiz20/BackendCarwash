package com.carwash.dto;

import com.carwash.model.EstadoReserva;
import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservaDTO {
    private Long id;

    @NotNull(message = "El cliente es requerido")
    private Long clienteId;

    @NotNull(message = "El servicio es requerido")
    private Long servicioId;

    private Long bahiaId;

    @NotNull(message = "La fecha de reserva es requerida")
    @Future(message = "La fecha debe ser futura")
    private LocalDateTime fechaReserva;

    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private EstadoReserva estado;

    @NotBlank(message = "La placa del vehículo es requerida")
    private String vehiculoPlaca;

    private String vehiculoModelo;
    private String codigoCupon;
    private String observaciones;

    // Para respuestas
    private String clienteNombre;
    private String servicioNombre;
    private String bahiaNumero;
}
