package com.carwash.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "cupones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    private String descripcion;

    @Enumerated(EnumType.STRING)
    private TipoDescuento tipoDescuento;

    private BigDecimal valorDescuento;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_expiracion")
    private LocalDateTime fechaExpiracion;

    private Integer usosMaximos;
    
    @Builder.Default
    private Integer usosActuales = 0;

    @Builder.Default
    private Boolean activo = true;

    @Builder.Default
    @OneToMany(mappedBy = "cupon")
    private Set<Reserva> reservas = new HashSet<>();
    
    // ========== CAMPOS PARA SISTEMA DE FIDELIZACIÓN ==========
    
    /**
     * ✅ CORRECCIÓN: Cambiado de "cliente" (Usuario) a "perfilCliente" (PerfilCliente)
     * Ahora apunta correctamente al perfil del cliente
     * NULL si es un cupón promocional general
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_cliente_id")
    private PerfilCliente perfilCliente;
    
    /**
     * Puntos que costó este cupón (0 si es promocional)
     */
    @Builder.Default
    @Column(name = "puntos_usados")
    private Integer puntosUsados = 0;
    
    /**
     * Tipo de origen del cupón
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_origen")
    @Builder.Default
    private TipoOrigenCupon tipoOrigen = TipoOrigenCupon.PROMOCIONAL;
    
    // ========== MÉTODOS DE NEGOCIO ==========
    
    /**
     * Verifica si el cupón es válido
     */
    public boolean esValido() {
        return activo && 
               LocalDateTime.now().isBefore(fechaExpiracion) &&
               LocalDateTime.now().isAfter(fechaInicio) &&
               (usosMaximos == null || usosActuales < usosMaximos);
    }
    
    /**
     * ✅ CORRECCIÓN: Actualizado para usar PerfilCliente en lugar de Usuario
     * Verifica si el cupón es válido para un cliente específico
     */
    public boolean esValidoParaCliente(PerfilCliente perfil) {
        if (!esValido()) return false;
        
        // Si es cupón de fidelización, debe ser del cliente
        if (tipoOrigen == TipoOrigenCupon.FIDELIZACION) {
            return perfilCliente != null && perfilCliente.getId().equals(perfil.getId());
        }
        
        // Si es promocional, cualquiera puede usarlo
        return true;
    }
    
    /**
     * Registra un uso del cupón
     */
    public void registrarUso() {
        if (usosMaximos != null) {
            this.usosActuales++;
            if (this.usosActuales >= this.usosMaximos) {
                this.activo = false;
            }
        }
    }
}