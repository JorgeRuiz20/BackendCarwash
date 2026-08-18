package com.carwash.model;

import lombok.Getter;

/**
 * Define los tipos de cupones disponibles para canje por puntos
 */
@Getter
public enum TipoCuponFidelizacion {
    DESCUENTO_5(5, 100, "Cupón 5% de descuento"),
    DESCUENTO_10(10, 200, "Cupón 10% de descuento"),
    DESCUENTO_20(20, 500, "Cupón 20% de descuento");
    
    private final int porcentaje;
    private final int puntosRequeridos;
    private final String descripcion;
    
    TipoCuponFidelizacion(int porcentaje, int puntosRequeridos, String descripcion) {
        this.porcentaje = porcentaje;
        this.puntosRequeridos = puntosRequeridos;
        this.descripcion = descripcion;
    }
    
    /**
     * Obtiene el tipo de cupón por puntos a canjear
     */
    public static TipoCuponFidelizacion getPorPuntos(int puntos) {
        for (TipoCuponFidelizacion tipo : values()) {
            if (tipo.puntosRequeridos == puntos) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("No existe un cupón para " + puntos + " puntos");
    }
    
    /**
     * Valida si los puntos son válidos para canje
     */
    public static boolean esPuntosValido(int puntos) {
        for (TipoCuponFidelizacion tipo : values()) {
            if (tipo.puntosRequeridos == puntos) {
                return true;
            }
        }
        return false;
    }
}