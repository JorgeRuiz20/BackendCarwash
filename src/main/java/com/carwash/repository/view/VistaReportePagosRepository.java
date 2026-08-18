package com.carwash.repository.view;

import com.carwash.model.view.VistaReportePagos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para la vista de reportes de pagos
 */
@Repository
public interface VistaReportePagosRepository extends JpaRepository<VistaReportePagos, Long> {
    
    /**
     * Obtener todos los pagos ordenados por fecha descendente
     */
    List<VistaReportePagos> findAllByOrderByFechaPagoDesc();
    
    /**
     * Obtener pagos por rango de fechas
     */
    @Query("SELECT v FROM VistaReportePagos v WHERE v.fechaPago BETWEEN :inicio AND :fin ORDER BY v.fechaPago DESC")
    List<VistaReportePagos> findByFechaPagoBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
    
    /**
     * Obtener pagos por método de pago
     */
    List<VistaReportePagos> findByMetodoPagoOrderByFechaPagoDesc(String metodoPago);
    
    /**
     * Obtener pagos por estado
     */
    List<VistaReportePagos> findByEstadoPagoOrderByFechaPagoDesc(String estadoPago);
    
    /**
     * ✅ CORREGIDO: Cambiar 'EXITOSO' por 'COMPLETADO'
     * Obtener pagos completados por rango de fechas
     */
    @Query("SELECT v FROM VistaReportePagos v WHERE v.estadoPago = 'COMPLETADO' AND v.fechaPago BETWEEN :inicio AND :fin ORDER BY v.fechaPago DESC")
    List<VistaReportePagos> findPagosCompletadosByFecha(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
    
    /**
     * Obtener pagos de un cliente específico
     */
    @Query("SELECT v FROM VistaReportePagos v WHERE v.clienteEmail = :email ORDER BY v.fechaPago DESC")
    List<VistaReportePagos> findByClienteEmail(@Param("email") String email);
    
    /**
     * Obtener pagos por tipo de servicio
     */
    @Query("SELECT v FROM VistaReportePagos v WHERE v.servicioTipo = :tipo ORDER BY v.fechaPago DESC")
    List<VistaReportePagos> findByServicioTipo(@Param("tipo") String tipo);
    
    /**
     * Obtener pagos con cupón aplicado
     */
    @Query("SELECT v FROM VistaReportePagos v WHERE v.cuponCodigo IS NOT NULL ORDER BY v.fechaPago DESC")
    List<VistaReportePagos> findPagosConCupon();
    
    /**
     * Obtener pagos mayores a un monto
     */
    @Query("SELECT v FROM VistaReportePagos v WHERE v.monto >= :montoMinimo ORDER BY v.monto DESC")
    List<VistaReportePagos> findByMontoMayorQue(@Param("montoMinimo") BigDecimal montoMinimo);
    
    /**
     * Obtener pagos de una bahía específica
     */
    @Query("SELECT v FROM VistaReportePagos v WHERE v.bahiaNumero = :bahiaNumero ORDER BY v.fechaPago DESC")
    List<VistaReportePagos> findByBahiaNumero(@Param("bahiaNumero") String bahiaNumero);
    
    /**
     * ✅ CORREGIDO: Cambiar 'EXITOSO' por 'COMPLETADO'
     * Calcular total de ingresos por rango de fechas y método de pago
     */
    @Query("SELECT SUM(v.monto) FROM VistaReportePagos v WHERE v.estadoPago = 'COMPLETADO' AND v.metodoPago = :metodo AND v.fechaPago BETWEEN :inicio AND :fin")
    BigDecimal calcularIngresosPorMetodo(@Param("metodo") String metodo, @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
}