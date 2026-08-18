package com.carwash.dto;

import com.carwash.model.*;
import lombok.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoDTO {
    private Long id;
    private Long reservaId;
    private Long clienteId;

    @NotNull(message = "El monto es requerido")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal monto;

    private BigDecimal descuento;
    private BigDecimal montoTotal;

    @NotNull(message = "El método de pago es requerido")
    private MetodoPago metodoPago;

    private EstadoPago estado;
    private LocalDateTime fechaPago;
    private String transaccionId;
    private String referencia;
}
