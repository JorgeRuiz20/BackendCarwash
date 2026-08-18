package com.carwash.dto;

import com.carwash.model.MetodoPago;
import lombok.*;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoCreateDTO {
    @NotNull(message = "La reserva es requerida")
    private Long reservaId;

    @NotNull(message = "El método de pago es requerido")
    private MetodoPago metodoPago;

    // Para tarjeta de crédito/débito
    private String numeroTarjeta;
    private String cvv;
    private String fechaExpiracion;

    // Para Yape/Plin
    private String numeroTelefono;
    private String codigo; // Máximo 5 dígitos

    @AssertTrue(message = "Datos de pago incompletos")
    public boolean isValidPaymentData() {
        if (metodoPago == null)
            return false;
        if (metodoPago == MetodoPago.TARJETA_CREDITO || metodoPago == MetodoPago.TARJETA_DEBITO) {
            return numeroTarjeta != null && cvv != null && fechaExpiracion != null;
        }
        if (metodoPago == MetodoPago.YAPE || metodoPago == MetodoPago.PLIN) {
            return numeroTelefono != null && codigo != null;
        }
        return true; // EFECTIVO no necesita datos
    }
}
