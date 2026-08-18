package com.carwash.repository.view;

import com.carwash.model.view.VistaReporteAlertasBahias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repositorio para la vista de reportes de alertas por bahías
 */
@Repository
public interface VistaReporteAlertasBahiasRepository extends JpaRepository<VistaReporteAlertasBahias, Long> {
    
    /**
     * Obtener bahías con alertas críticas
     */
    @Query("SELECT v FROM VistaReporteAlertasBahias v WHERE v.totalAlertasCriticas > 0 ORDER BY v.totalAlertasCriticas DESC")
    List<VistaReporteAlertasBahias> findBahiasConAlertasCriticas();
    
    /**
     * Obtener todas las bahías ordenadas por total de alertas
     */
    @Query("SELECT v FROM VistaReporteAlertasBahias v ORDER BY v.totalAlertas DESC")
    List<VistaReporteAlertasBahias> findAllOrderByTotalAlertas();
    
    /**
     * Obtener bahías con más de X alertas
     */
    @Query("SELECT v FROM VistaReporteAlertasBahias v WHERE v.totalAlertas > :minAlertas ORDER BY v.totalAlertas DESC")
    List<VistaReporteAlertasBahias> findBahiasConMinimoAlertas(Long minAlertas);
    
    /**
     * Obtener bahías en mantenimiento con alertas
     */
    @Query("SELECT v FROM VistaReporteAlertasBahias v WHERE v.bahiaEstado = 'MANTENIMIENTO' AND v.totalAlertas > 0")
    List<VistaReporteAlertasBahias> findBahiasEnMantenimientoConAlertas();
    
    /**
     * Obtener bahías por número
     */
    VistaReporteAlertasBahias findByBahiaNumero(String bahiaNumero);
}