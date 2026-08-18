package com.carwash.service;

import com.carwash.dto.ReporteDTO;
import com.carwash.model.*;
import com.carwash.repository.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteService {
    private final ReporteRepository reporteRepository;
    private final ReservaRepository reservaRepository;
    private final PagoRepository pagoRepository;
    private final SensorTelemetriaRepository sensorRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public ReporteDTO generarReporteDiario(LocalDate fecha) {
        LocalDateTime inicioDay = fecha.atStartOfDay();
        LocalDateTime finDia = fecha.atTime(23, 59, 59);

        List<Reserva> reservas = reservaRepository.findByFechaReservaBetween(inicioDay, finDia);
        BigDecimal ingresos = pagoRepository.calcularIngresosPeriodo(inicioDay, finDia);
        Double consumoAgua = sensorRepository.calcularPromedioSensor(
                TipoSensor.FLUJO_AGUA, inicioDay, finDia);
        Double consumoEnergia = sensorRepository.calcularPromedioSensor(
                TipoSensor.CONSUMO_ENERGIA, inicioDay, finDia);

        Reporte reporte = Reporte.builder()
                .tipo(TipoReporte.DIARIO)
                .fechaInicio(fecha)
                .fechaFin(fecha)
                .totalReservas(reservas.size())
                .ingresoTotal(ingresos != null ? ingresos : BigDecimal.ZERO)
                .consumoAgua(consumoAgua != null ? consumoAgua : 0.0)
                .consumoEnergia(consumoEnergia != null ? consumoEnergia : 0.0)
                .fechaGeneracion(LocalDate.now())
                .build();

        reporte = reporteRepository.save(reporte);
        return modelMapper.map(reporte, ReporteDTO.class);
    }

    @Transactional
    public ReporteDTO generarReporteMensual(int mes, int anio) {
        LocalDate inicioMes = LocalDate.of(anio, mes, 1);
        LocalDate finMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());

        LocalDateTime inicio = inicioMes.atStartOfDay();
        LocalDateTime fin = finMes.atTime(23, 59, 59);

        List<Reserva> reservas = reservaRepository.findByFechaReservaBetween(inicio, fin);
        BigDecimal ingresos = pagoRepository.calcularIngresosPeriodo(inicio, fin);
        Double consumoAgua = sensorRepository.calcularPromedioSensor(
                TipoSensor.FLUJO_AGUA, inicio, fin);
        Double consumoEnergia = sensorRepository.calcularPromedioSensor(
                TipoSensor.CONSUMO_ENERGIA, inicio, fin);

        Reporte reporte = Reporte.builder()
                .tipo(TipoReporte.MENSUAL)
                .fechaInicio(inicioMes)
                .fechaFin(finMes)
                .totalReservas(reservas.size())
                .ingresoTotal(ingresos != null ? ingresos : BigDecimal.ZERO)
                .consumoAgua(consumoAgua != null ? consumoAgua : 0.0)
                .consumoEnergia(consumoEnergia != null ? consumoEnergia : 0.0)
                .fechaGeneracion(LocalDate.now())
                .build();

        reporte = reporteRepository.save(reporte);
        return modelMapper.map(reporte, ReporteDTO.class);
    }

    public List<ReporteDTO> listarReportes(TipoReporte tipo) {
        return reporteRepository.findUltimosReportesByTipo(tipo).stream()
                .limit(10)
                .map(r -> modelMapper.map(r, ReporteDTO.class))
                .collect(Collectors.toList());
    }
}
