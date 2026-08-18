package com.carwash.repository.view;

import com.carwash.model.view.VistaResumenPagosDiario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la vista de resumen diario de pagos
 */
@Repository
public interface VistaResumenPagosDiarioRepository extends JpaRepository<VistaResumenPagosDiario, LocalDate> {
    
    /**
     * Obtener resumen de un día específico
     */
    Optional<VistaResumenPagosDiario> findByFecha(LocalDate fecha);
    
    /**
     * Obtener resúmenes por rango de fechas ordenados descendentemente
     */
    @Query("SELECT v FROM VistaResumenPagosDiario v WHERE v.fecha BETWEEN :inicio AND :fin ORDER BY v.fecha DESC")
    List<VistaResumenPagosDiario> findByFechaBetween(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);
    
    /**
     * Obtener los últimos N días de resumen
     */
    @Query("SELECT v FROM VistaResumenPagosDiario v ORDER BY v.fecha DESC")
    List<VistaResumenPagosDiario> findUltimosDias();
    
    /**
     * Obtener días con ingresos mayores a un monto
     */
    @Query("SELECT v FROM VistaResumenPagosDiario v WHERE v.ingresosTotales >= :montoMinimo ORDER BY v.fecha DESC")
    List<VistaResumenPagosDiario> findDiasConIngresosMayores(@Param("montoMinimo") BigDecimal montoMinimo);
    
    /**
     * Obtener días del mes actual
     */
    @Query("SELECT v FROM VistaResumenPagosDiario v WHERE YEAR(v.fecha) = :anio AND MONTH(v.fecha) = :mes ORDER BY v.fecha DESC")
    List<VistaResumenPagosDiario> findByMesAnio(@Param("mes") int mes, @Param("anio") int anio);
    
    /**
     * Calcular total de ingresos por rango de fechas
     */
    @Query("SELECT SUM(v.ingresosTotales) FROM VistaResumenPagosDiario v WHERE v.fecha BETWEEN :inicio AND :fin")
    BigDecimal calcularIngresosTotalesPeriodo(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);
    
    /**
     * Calcular promedio de ingresos diarios por período
     */
    @Query("SELECT AVG(v.ingresosTotales) FROM VistaResumenPagosDiario v WHERE v.fecha BETWEEN :inicio AND :fin")
    BigDecimal calcularPromedioIngresosDiarios(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);
    
    /**
     * Obtener día con mayores ingresos en un período
     */
    @Query("SELECT v FROM VistaResumenPagosDiario v WHERE v.fecha BETWEEN :inicio AND :fin ORDER BY v.ingresosTotales DESC")
    List<VistaResumenPagosDiario> findDiaMayorIngreso(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);
}