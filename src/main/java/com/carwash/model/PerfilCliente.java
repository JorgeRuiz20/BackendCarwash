package com.carwash.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "perfiles_cliente")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilCliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Builder.Default
    private Integer puntosFidelidad = 0;
    
    @Builder.Default
    private Integer totalServicios = 0;
    
    private String vehiculoFavorito;
    private String preferenciaLavado;
    
    // ========== CAMPOS PARA SISTEMA DE FIDELIZACIÓN ==========
    
    /**
     * Total gastado por el cliente en todos sus servicios
     */
    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalGastado = BigDecimal.ZERO;
    
    /**
     * ✅ CORRECCIÓN: Cambiado mappedBy de "cliente" a "perfilCliente"
     * para que coincida con la propiedad en la entidad Cupon
     */
    @OneToMany(mappedBy = "perfilCliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Cupon> cuponesFidelizacion = new ArrayList<>();
    
    // ========== MÉTODOS DE NEGOCIO ==========
    
    /**
     * Agrega puntos basado en el monto gastado
     * Regla: 1 Sol = 10 puntos
     */
    public void agregarPuntosPorCompra(BigDecimal monto) {
        int puntosGanados = monto.intValue() * 10;
        this.puntosFidelidad += puntosGanados;
        this.totalGastado = this.totalGastado.add(monto);
        this.totalServicios++;
    }
    
    /**
     * Descuenta puntos al canjear un cupón
     */
    public void descontarPuntos(int puntos) {
        if (this.puntosFidelidad < puntos) {
            throw new IllegalStateException("Puntos insuficientes");
        }
        this.puntosFidelidad -= puntos;
    }
    
    /**
     * Verifica si tiene puntos suficientes
     */
    public boolean tienePuntosSuficientes(int puntosRequeridos) {
        return this.puntosFidelidad >= puntosRequeridos;
    }
    
    /**
     * Obtiene cupones de fidelización activos y válidos
     */
    public List<Cupon> getCuponesActivosFidelizacion() {
        return cuponesFidelizacion.stream()
                .filter(Cupon::esValido)
                .toList();
    }
}