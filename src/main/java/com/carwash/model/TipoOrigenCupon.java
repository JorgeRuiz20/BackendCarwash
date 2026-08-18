package com.carwash.model;

/**
 * Define el origen de un cupón
 */
public enum TipoOrigenCupon {
    /**
     * Cupón creado por el administrador como promoción general
     * Puede ser usado por cualquier cliente que conozca el código
     */
    PROMOCIONAL,
    
    /**
     * Cupón generado por canje de puntos de fidelización
     * Solo puede ser usado por el cliente que lo canjeó
     */
    FIDELIZACION
}