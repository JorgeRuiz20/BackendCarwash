package com.carwash.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "reserva_id")
    private Reserva reserva;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Usuario cliente;

    @Column(nullable = false)
    private BigDecimal monto;

    @Builder.Default
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(name = "monto_total", nullable = false)
    private BigDecimal montoTotal;

    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;

    @Enumerated(EnumType.STRING)
    private EstadoPago estado;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    private String transaccionId;
    private String referencia;
    
    // ========== NUEVOS CAMPOS PARA SISTEMA DE FIDELIZACIÓN ==========
    
    /**
     * Indica si ya se acreditaron los puntos por este pago
     */
    @Builder.Default
    @Column(name = "puntos_acreditados")
    private Boolean puntosAcreditados = false;
    
    /**
     * Cantidad de puntos ganados con este pago
     */
    @Builder.Default
    @Column(name = "puntos_ganados")
    private Integer puntosGanados = 0;
    
    /**
     * ID del cupón aplicado (si existe)
     * Se mantiene como referencia incluso después de usar el cupón
     */
    @Column(name = "cupon_aplicado_id")
    private Long cuponAplicadoId;
    
    /**
     * Código del cupón aplicado (para histórico)
     */
    @Column(name = "cupon_codigo")
    private String cuponCodigo;
    
    /**
     * Porcentaje de descuento aplicado
     */
    @Column(name = "porcentaje_descuento")
    private Integer porcentajeDescuento;
}