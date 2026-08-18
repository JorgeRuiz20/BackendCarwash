package com.carwash.service;

import com.carwash.model.TarjetaCredito;
import com.carwash.repository.TarjetaCreditoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TarjetaCreditoService {
    private final TarjetaCreditoRepository tarjetaRepository;

    public boolean validarTarjeta(String numero, String cvv, String fechaExp) {
        return tarjetaRepository.findByNumeroTarjetaAndCvvAndFechaExpiracion(
                numero, cvv, fechaExp).map(t -> t.getActiva()).orElse(false);
    }
}
