package com.carwash.model.view;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import java.time.LocalDateTime;

/**
 * Vista SQL para reportes de alertas agrupadas por bahías
 * Corresponde a la vista: vista_reporte_alertas_bahias
 */
@Entity
@Immutable
@Subselect("SELECT * FROM vista_reporte_alertas_bahias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VistaReporteAlertasBahias {

    @Id
    @Column(name = "bahia_id")
    private Long bahiaId;

    @Column(name = "bahia_numero")
    private String bahiaNumero;

    @Column(name = "bahia_nombre")
    private String bahiaNombre;

    @Column(name = "bahia_estado")
    private String bahiaEstado;

    @Column(name = "total_alertas_criticas")
    private Long totalAlertasCriticas;

    @Column(name = "total_alertas_advertencia")
    private Long totalAlertasAdvertencia;

    @Column(name = "total_alertas")
    private Long totalAlertas;

    @Column(name = "ultima_alerta_fecha")
    private LocalDateTime ultimaAlertaFecha;

    @Column(name = "sensores_afectados")
    private String sensoresAfectados;

    @Column(name = "promedio_nivel_agua")
    private Double promedioNivelAgua;

    @Column(name = "promedio_presion_agua")
    private Double promedioPresionAgua;

    @Column(name = "promedio_temperatura")
    private Double promedioTemperatura;

    @Column(name = "promedio_flujo_agua")
    private Double promedioFlujoAgua;

    @Column(name = "promedio_consumo_energia")
    private Double promedioConsumoEnergia;
}
