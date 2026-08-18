package com.carwash.repository;

import com.carwash.model.Pago;
import com.carwash.model.Usuario;
import com.carwash.model.EstadoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByCliente(Usuario cliente);

    List<Pago> findByEstado(EstadoPago estado);

    Optional<Pago> findByReservaId(Long reservaId);

    Optional<Pago> findByTransaccionId(String transaccionId);

    @Query("SELECT SUM(p.montoTotal) FROM Pago p WHERE p.estado = 'COMPLETADO' AND DATE(p.fechaPago) = DATE(:fecha)")
    BigDecimal calcularIngresosDia(@Param("fecha") LocalDateTime fecha);

    @Query("SELECT SUM(p.montoTotal) FROM Pago p WHERE p.estado = 'COMPLETADO' AND p.fechaPago BETWEEN :inicio AND :fin")
    BigDecimal calcularIngresosPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(p) FROM Pago p WHERE p.estado = 'COMPLETADO' AND DATE(p.fechaPago) = DATE(:fecha)")
    Long countPagosCompletadosHoy(@Param("fecha") LocalDateTime fecha);
}
