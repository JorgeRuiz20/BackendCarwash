package com.carwash.model.view;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Vista SQL para resumen diario de pagos y métricas
 * Corresponde a la vista: vista_resumen_pagos_diario
 * 
 * NOTA: Esta clase ha sido corregida para usar "pagosCompletados" en lugar de "pagosExitosos"
 * para coincidir con el enum EstadoPago que usa COMPLETADO, no EXITOSO
 */
@Entity
@Immutable
@Subselect("SELECT * FROM vista_resumen_pagos_diario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VistaResumenPagosDiario {

    @Id
    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "total_pagos")
    private Long totalPagos;

    // ✅ CORREGIDO: Cambiado de "pagos_exitosos" a "pagos_completados"
    // para coincidir con el enum EstadoPago.COMPLETADO
    @Column(name = "pagos_completados")
    private Long pagosCompletados;

    @Column(name = "pagos_pendientes")
    private Long pagosPendientes;

    @Column(name = "pagos_fallidos")
    private Long pagosFallidos;

    @Column(name = "ingresos_totales")
    private BigDecimal ingresosTotales;

    @Column(name = "ticket_promedio")
    private BigDecimal ticketPromedio;

    @Column(name = "ticket_maximo")
    private BigDecimal ticketMaximo;

    @Column(name = "ticket_minimo")
    private BigDecimal ticketMinimo;

    @Column(name = "pagos_tarjeta")
    private Long pagosTarjeta;

    @Column(name = "pagos_yape")
    private Long pagosYape;

    @Column(name = "pagos_efectivo")
    private Long pagosEfectivo;

    @Column(name = "ingresos_tarjeta")
    private BigDecimal ingresosTarjeta;

    @Column(name = "ingresos_yape")
    private BigDecimal ingresosYape;

    @Column(name = "ingresos_efectivo")
    private BigDecimal ingresosEfectivo;

    @Column(name = "clientes_unicos")
    private Long clientesUnicos;

    @Column(name = "servicios_distintos")
    private Long serviciosDistintos;

    @Column(name = "bahias_utilizadas")
    private Long bahiasUtilizadas;

    @Column(name = "descuentos_totales")
    private BigDecimal descuentosTotales;

    @Column(name = "reservas_con_cupon")
    private Long reservasConCupon;

    @Column(name = "duracion_promedio_servicio")
    private Double duracionPromedioServicio;

    @Column(name = "servicios_basico")
    private Long serviciosBasico;

    @Column(name = "servicios_completo")
    private Long serviciosCompleto;

    @Column(name = "servicios_premium")
    private Long serviciosPremium;

    @Column(name = "servicios_express")
    private Long serviciosExpress;
}