package com.carwash.model.view;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Vista SQL para reportes detallados de pagos
 * Corresponde a la vista: vista_reporte_pagos
 */
@Entity
@Immutable
@Subselect("SELECT * FROM vista_reporte_pagos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VistaReportePagos {

    @Id
    @Column(name = "pago_id")
    private Long pagoId;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @Column(name = "monto")
    private BigDecimal monto;

    @Column(name = "metodo_pago")
    private String metodoPago;

    @Column(name = "estado_pago")
    private String estadoPago;

    @Column(name = "numero_transaccion")
    private String numeroTransaccion;

    @Column(name = "reserva_id")
    private Long reservaId;

    @Column(name = "reserva_estado")
    private String reservaEstado;

    @Column(name = "fecha_reserva")
    private LocalDateTime fechaReserva;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Column(name = "precio_servicio")
    private BigDecimal precioServicio;

    @Column(name = "descuento_aplicado")
    private BigDecimal descuentoAplicado;

    @Column(name = "precio_total")
    private BigDecimal precioTotal;

    @Column(name = "vehiculo_placa")
    private String vehiculoPlaca;

    @Column(name = "vehiculo_modelo")
    private String vehiculoModelo;

    @Column(name = "cliente_nombre")
    private String clienteNombre;

    @Column(name = "cliente_email")
    private String clienteEmail;

    @Column(name = "cliente_telefono")
    private String clienteTelefono;

    @Column(name = "servicio_id")
    private Long servicioId;

    @Column(name = "servicio_nombre")
    private String servicioNombre;

    @Column(name = "servicio_tipo")
    private String servicioTipo;

    @Column(name = "servicio_duracion")
    private Integer servicioDuracion;

    @Column(name = "bahia_numero")
    private String bahiaNumero;

    @Column(name = "bahia_nombre")
    private String bahiaNombre;

    @Column(name = "cupon_codigo")
    private String cuponCodigo;

    @Column(name = "cupon_tipo")
    private String cuponTipo;

    @Column(name = "cupon_valor")
    private BigDecimal cuponValor;

    @Column(name = "duracion_real_minutos")
    private Integer duracionRealMinutos;
}
