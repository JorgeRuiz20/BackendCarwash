package com.carwash.repository;

import com.carwash.model.Bahia;
import com.carwash.model.EstadoBahia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BahiaRepository extends JpaRepository<Bahia, Long> {
    Optional<Bahia> findByNumero(String numero);

    List<Bahia> findByEstado(EstadoBahia estado);

    List<Bahia> findByDisponibleTrue();

    @Query("SELECT b FROM Bahia b WHERE b.disponible = true AND b.estado = 'DISPONIBLE'")
    List<Bahia> findBahiasDisponibles();

    @Query("SELECT COUNT(b) FROM Bahia b WHERE b.estado = 'DISPONIBLE'")
    Long countBahiasDisponibles();
}
