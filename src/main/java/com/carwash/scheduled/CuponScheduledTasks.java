package com.carwash.scheduled;

import com.carwash.service.CuponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Tarea programada para desactivar cupones expirados
 * Se ejecuta diariamente a las 2:00 AM
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CuponScheduledTasks {
    
    private final CuponService cuponService;
    
    @Scheduled(cron = "0 0 2 * * *") // Todos los días a las 2:00 AM
    public void desactivarCuponesExpirados() {
        log.info("Iniciando tarea programada: desactivar cupones expirados");
        try {
            cuponService.desactivarCuponesExpirados();
            log.info("Tarea completada: cupones expirados desactivados");
        } catch (Exception e) {
            log.error("Error al desactivar cupones expirados: {}", e.getMessage(), e);
        }
    }
}