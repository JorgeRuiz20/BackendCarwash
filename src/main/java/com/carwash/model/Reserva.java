package com.carwash.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "reservas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @ManyToOne
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    @ManyToOne
    @JoinColumn(name = "bahia_id")
    private Bahia bahia;

    @Column(name = "fecha_reserva", nullable = false)
    private LocalDateTime fechaReserva;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    private EstadoReserva estado;

    private String vehiculoPlaca;
    private String vehiculoModelo;

    @ManyToOne
    @JoinColumn(name = "cupon_id")
    private Cupon cupon;

    @OneToOne(mappedBy = "reserva", cascade = CascadeType.ALL)
    private Pago pago;

    private String observaciones;

    @Column(name = "precio_servicio")
    private BigDecimal precioServicio;

    @Column(name = "descuento_aplicado")
    private BigDecimal descuentoAplicado;

    @Column(name = "precio_total")
    private BigDecimal precioTotal;
}