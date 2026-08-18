package com.carwash.repository;

import com.carwash.model.Reporte;
import com.carwash.model.TipoReporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Long> {
    List<Reporte> findByTipo(TipoReporte tipo);

    @Query("SELECT r FROM Reporte r WHERE r.fechaInicio >= :inicio AND r.fechaFin <= :fin")
    List<Reporte> findByPeriodo(
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin);

    @Query("SELECT r FROM Reporte r WHERE r.tipo = :tipo ORDER BY r.fechaGeneracion DESC")
    List<Reporte> findUltimosReportesByTipo(@Param("tipo") TipoReporte tipo);
}
