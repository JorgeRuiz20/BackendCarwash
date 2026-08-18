package com.carwash.model;

public enum NivelAlerta {
    NORMAL, // Todo bien, no hay alerta
    ADVERTENCIA, // Hay alerta pero no crítica (operador decide)
    CRITICA // Alerta crítica (detiene automáticamente)
}
