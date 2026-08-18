package com.carwash.repository;

import com.carwash.model.CodigoYape;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CodigoYapeRepository extends JpaRepository<CodigoYape, Long> {
    boolean existsByUsuarioIdAndCodigoAndUsadoTrue(Long usuarioId, String codigo);
}
