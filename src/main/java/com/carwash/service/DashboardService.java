package com.carwash.service;

import com.carwash.dto.*;
import com.carwash.model.EstadoReserva;
import com.carwash.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final ReservaRepository reservaRepository;
    private final BahiaRepository bahiaRepository;
    private final PagoRepository pagoRepository;
    private final SensorTelemetriaRepository sensorRepository;
    private final BahiaService bahiaService;
    private final ReservaService reservaService;
    private final SensorTelemetriaService sensorService;
    private final PagoService pagoService;

    public DashboardDTO obtenerDashboard() {
        LocalDateTime hoy = LocalDateTime.now();

        return DashboardDTO.builder()
                .reservasHoy(reservaRepository.findReservasActivasDelDia(hoy).size())
                .reservasPendientes(reservaRepository.countByEstado(EstadoReserva.PENDIENTE).intValue())
                .reservasEnProgreso(reservaRepository.countByEstado(EstadoReserva.EN_PROGRESO).intValue())
                .ingresosHoy(pagoService.calcularIngresosHoy())
                .ingresosMes(pagoService.calcularIngresosMes())
                .bahiasDisponibles(bahiaService.listarDisponibles())
                .proximasReservas(reservaService.listarReservasHoy().stream()
                        .limit(5)
                        .collect(Collectors.toList()))
                .alertasActivas(sensorService.obtenerAlertasActivas())
                .build();
    }
}
