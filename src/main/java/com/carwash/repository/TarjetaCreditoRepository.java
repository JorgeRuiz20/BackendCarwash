package com.carwash.repository;

import com.carwash.model.TarjetaCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TarjetaCreditoRepository extends JpaRepository<TarjetaCredito, Long> {
    Optional<TarjetaCredito> findByNumeroTarjetaAndCvvAndFechaExpiracion(
            String numeroTarjeta, String cvv, String fechaExpiracion);
}
