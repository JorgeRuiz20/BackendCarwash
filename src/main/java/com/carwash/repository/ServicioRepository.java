package com.carwash.repository;

import com.carwash.model.Servicio;
import com.carwash.model.TipoServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {
    List<Servicio> findByActivoTrue();

    List<Servicio> findByTipo(TipoServicio tipo);

    List<Servicio> findByTipoAndActivoTrue(TipoServicio tipo);
}
