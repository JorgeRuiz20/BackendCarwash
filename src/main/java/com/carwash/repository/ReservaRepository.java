package com.carwash.repository;

import com.carwash.model.Reserva;
import com.carwash.model.Usuario;
import com.carwash.model.EstadoReserva;
import com.carwash.model.Bahia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
        List<Reserva> findByCliente(Usuario cliente);

        List<Reserva> findByEstado(EstadoReserva estado);

        List<Reserva> findByBahia(Bahia bahia);

        @Query("SELECT r FROM Reserva r WHERE r.cliente.id = :clienteId ORDER BY r.fechaReserva DESC")
        List<Reserva> findByClienteIdOrderByFechaDesc(@Param("clienteId") Long clienteId);

        @Query("SELECT r FROM Reserva r WHERE r.estado = :estado ORDER BY r.fechaReserva ASC")
        List<Reserva> findByEstadoOrderByFecha(@Param("estado") EstadoReserva estado);

        @Query("SELECT r FROM Reserva r WHERE r.fechaReserva BETWEEN :inicio AND :fin")
        List<Reserva> findByFechaReservaBetween(
                        @Param("inicio") LocalDateTime inicio,
                        @Param("fin") LocalDateTime fin);

        @Query("SELECT r FROM Reserva r WHERE DATE(r.fechaReserva) = DATE(:fecha) AND r.estado IN ('PENDIENTE', 'CONFIRMADA', 'EN_PROGRESO')")
        List<Reserva> findReservasActivasDelDia(@Param("fecha") LocalDateTime fecha);

        @Query("SELECT COUNT(r) FROM Reserva r WHERE r.estado = :estado")
        Long countByEstado(@Param("estado") EstadoReserva estado);

        @Query("SELECT r FROM Reserva r WHERE r.bahia.id = :bahiaId AND r.fechaReserva BETWEEN :inicio AND :fin AND r.estado NOT IN ('CANCELADA', 'COMPLETADA')")
        List<Reserva> findConflictingReservations(
                        @Param("bahiaId") Long bahiaId,
                        @Param("inicio") LocalDateTime inicio,
                        @Param("fin") LocalDateTime fin);

        // NUEVOS MÉTODOS PARA ALERTAS
        List<Reserva> findByBahiaAndEstadoIn(Bahia bahia, List<EstadoReserva> estados);

        List<Reserva> findByBahiaAndEstado(Bahia bahia, EstadoReserva estado);
}
