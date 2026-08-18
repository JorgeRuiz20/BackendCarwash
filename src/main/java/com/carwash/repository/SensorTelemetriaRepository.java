package com.carwash.repository;

import com.carwash.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface SensorTelemetriaRepository extends JpaRepository<SensorTelemetria, Long> {
    
    List<SensorTelemetria> findByBahiaOrderByFechaLecturaDesc(Bahia bahia);
    
    List<SensorTelemetria> findByAlertaActivaTrue();
    
    // ✅ MÉTODO CORREGIDO: Agregado para AlertaService
    List<SensorTelemetria> findByAlertaActivaTrueAndAlertaResueltaFalse();
    
    // ✅ MÉTODO CORREGIDO: Agregado para AlertaService
    List<SensorTelemetria> findByNivelAlertaAndAlertaResueltaFalse(NivelAlerta nivel);
    
    @Query("SELECT s FROM SensorTelemetria s WHERE s.bahia.id = :bahiaId ORDER BY s.fechaLectura DESC")
    List<SensorTelemetria> findUltimasLecturasByBahia(@Param("bahiaId") Long bahiaId);
    
    @Query("SELECT AVG(s.valor) FROM SensorTelemetria s WHERE s.tipoSensor = :tipo " +
           "AND s.fechaLectura BETWEEN :inicio AND :fin")
    Double calcularPromedioSensor(
        @Param("tipo") TipoSensor tipo,
        @Param("inicio") LocalDateTime inicio,
        @Param("fin") LocalDateTime fin
    );
    
    // ✅ NUEVO MÉTODO AGREGADO PARA SOLUCIONAR EL PROBLEMA DE ALERTAS EN TELEMETRÍA
    /**
     * Verifica si existe al menos una alerta activa, sin resolver, de un nivel específico
     * para una bahía determinada.
     * 
     * Este método se usa en TelemetriaAutomaticaService para evitar generar nuevos datos
     * de telemetría mientras haya alertas críticas sin resolver.
     * 
     * @param bahia La bahía a verificar
     * @param nivelAlerta El nivel de alerta a buscar (CRITICA, ADVERTENCIA, etc.)
     * @return true si existe al menos una alerta que cumpla las condiciones
     */
    boolean existsByBahiaAndAlertaActivaTrueAndAlertaResueltaFalseAndNivelAlerta(
        Bahia bahia, 
        NivelAlerta nivelAlerta
    );
}