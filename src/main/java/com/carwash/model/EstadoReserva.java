package com.carwash.model;

public enum EstadoReserva {
    PENDIENTE,
    CONFIRMADA,
    EN_PROGRESO,
    EN_ESPERA, // Detenida por alerta
    COMPLETADA,
    CANCELADA
}