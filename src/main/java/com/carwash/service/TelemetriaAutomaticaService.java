package com.carwash.service;

import com.carwash.model.*;
import com.carwash.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelemetriaAutomaticaService {
    private final SensorTelemetriaRepository sensorRepository;
    private final BahiaRepository bahiaRepository;
    private final ReservaRepository reservaRepository;

    /**
     * ✅ MÉTODO CORREGIDO
     * Genera datos de telemetría cada 15 segundos SOLO para bahías que:
     * 1. Están en estado EN_USO
     * 2. NO tienen alertas críticas sin resolver
     */
    @Scheduled(fixedRate = 15000) // Cada 15 segundos
    @Transactional
    public void generarDatosTelemetria() {
        List<Bahia> bahias = bahiaRepository.findAll();
        LocalDateTime ahora = LocalDateTime.now();

        for (Bahia bahia : bahias) {
            // ✅ Solo generar si está EN_USO
            if (bahia.getEstado() == EstadoBahia.EN_USO) {
                
                // ✅ CORRECCIÓN: Verificar si hay alertas críticas sin resolver
                boolean tieneAlertaCritica = sensorRepository
                    .existsByBahiaAndAlertaActivaTrueAndAlertaResueltaFalseAndNivelAlerta(
                        bahia, NivelAlerta.CRITICA
                    );
                
                if (tieneAlertaCritica) {
                    log.warn("⏸️ Bahía {}: Alerta crítica activa - telemetría suspendida hasta resolución", 
                        bahia.getNumero());
                    
                    // ✅ Seguridad: verificar estado de la bahía (por si hay inconsistencia)
                    if (bahia.getEstado() != EstadoBahia.MANTENIMIENTO) {
                        log.error("🔧 INCONSISTENCIA DETECTADA: Bahía {} tiene alerta crítica pero estado={}", 
                            bahia.getNumero(), bahia.getEstado());
                        log.error("🔧 Corrigiendo estado a MANTENIMIENTO...");
                        bahia.setEstado(EstadoBahia.MANTENIMIENTO);
                        bahia.setDisponible(false);
                        bahiaRepository.save(bahia);
                    }
                    continue; // No generar datos para esta bahía
                }
                
                // Todo OK, generar lecturas normales
                generarLecturasPorBahia(bahia, ahora);
            }
        }
    }

    private void generarLecturasPorBahia(Bahia bahia, LocalDateTime ahora) {
        Random random = new Random();

        // Nivel de agua (50-100%)
        double nivelAgua = 50 + (random.nextDouble() * 50);
        procesarSensor(bahia, TipoSensor.NIVEL_AGUA, nivelAgua, "%", ahora);

        // Presión de agua (2.0-5.0 bar)
        double presionAgua = 2.0 + (random.nextDouble() * 3.0);
        procesarSensor(bahia, TipoSensor.PRESION_AGUA, presionAgua, "bar", ahora);

        // Temperatura (20-45°C)
        double temperatura = 20 + (random.nextDouble() * 25);
        procesarSensor(bahia, TipoSensor.TEMPERATURA, temperatura, "°C", ahora);

        // Flujo de agua (30-80 L/min)
        double flujoAgua = 30 + (random.nextDouble() * 50);
        procesarSensor(bahia, TipoSensor.FLUJO_AGUA, flujoAgua, "L/min", ahora);

        // Consumo energía (5-15 kWh)
        double consumoEnergia = 5 + (random.nextDouble() * 10);
        procesarSensor(bahia, TipoSensor.CONSUMO_ENERGIA, consumoEnergia, "kWh", ahora);
    }

    private void procesarSensor(Bahia bahia, TipoSensor tipo, double valor,
            String unidad, LocalDateTime fecha) {
        SensorTelemetria sensor = SensorTelemetria.builder()
                .bahia(bahia)
                .tipoSensor(tipo)
                .valor(valor)
                .unidad(unidad)
                .fechaLectura(fecha)
                .alertaActiva(false)
                .nivelAlerta(NivelAlerta.NORMAL)
                .alertaResuelta(false)
                .build();

        // Verificar umbrales y clasificar gravedad
        verificarUmbrales(sensor);

        // Guardar sensor
        sensorRepository.save(sensor);

        // ACCIÓN AUTOMÁTICA SI ES CRÍTICA
        if (sensor.getNivelAlerta() == NivelAlerta.CRITICA) {
            detenerServicioAutomaticamente(bahia, sensor);
        } else if (sensor.getAlertaActiva()) {
            log.warn("⚠️ ADVERTENCIA en Bahía {}: {}",
                    bahia.getNumero(), sensor.getMensajeAlerta());
        }
    }

    private void verificarUmbrales(SensorTelemetria sensor) {
        switch (sensor.getTipoSensor()) {
            case NIVEL_AGUA:
                if (sensor.getValor() < 15.0) {
                    // CRÍTICA
                    sensor.setAlertaActiva(true);
                    sensor.setNivelAlerta(NivelAlerta.CRITICA);
                    sensor.setMensajeAlerta("🔴 CRÍTICO: Nivel agua < 15% - SERVICIO DETENIDO");
                } else if (sensor.getValor() < 20.0) {
                    // CRÍTICA
                    sensor.setAlertaActiva(true);
                    sensor.setNivelAlerta(NivelAlerta.CRITICA);
                    sensor.setMensajeAlerta("🔴 NIVEL CRÍTICO: Agua < 20% - SERVICIO DETENIDO");
                } else if (sensor.getValor() < 30.0) {
                    // ADVERTENCIA
                    sensor.setAlertaActiva(true);
                    sensor.setNivelAlerta(NivelAlerta.ADVERTENCIA);
                    sensor.setMensajeAlerta("🟡 ADVERTENCIA: Nivel agua < 30%");
                }
                break;

            case PRESION_AGUA:
                if (sensor.getValor() < 1.5) {
                    // CRÍTICA
                    sensor.setAlertaActiva(true);
                    sensor.setNivelAlerta(NivelAlerta.CRITICA);
                    sensor.setMensajeAlerta("🔴 PRESIÓN CRÍTICA: < 1.5 bar - SERVICIO DETENIDO");
                } else if (sensor.getValor() > 5.5) {
                    // CRÍTICA
                    sensor.setAlertaActiva(true);
                    sensor.setNivelAlerta(NivelAlerta.CRITICA);
                    sensor.setMensajeAlerta("🔴 PRESIÓN PELIGROSA: > 5.5 bar - RIESGO DE DAÑOS");
                } else if (sensor.getValor() < 2.0 || sensor.getValor() > 5.0) {
                    // ADVERTENCIA
                    sensor.setAlertaActiva(true);
                    sensor.setNivelAlerta(NivelAlerta.ADVERTENCIA);
                    sensor.setMensajeAlerta("🟡 Presión fuera de rango óptimo");
                }
                break;

            case TEMPERATURA:
                if (sensor.getValor() > 50.0) {
                    // CRÍTICA
                    sensor.setAlertaActiva(true);
                    sensor.setNivelAlerta(NivelAlerta.CRITICA);
                    sensor.setMensajeAlerta("🔴 TEMPERATURA PELIGROSA: > 50°C - SERVICIO DETENIDO");
                } else if (sensor.getValor() > 45.0) {
                    // ADVERTENCIA
                    sensor.setAlertaActiva(true);
                    sensor.setNivelAlerta(NivelAlerta.ADVERTENCIA);
                    sensor.setMensajeAlerta("🟡 Temperatura elevada: > 45°C");
                }
                break;

            case FLUJO_AGUA:
                if (sensor.getValor() < 25.0) {
                    // CRÍTICA
                    sensor.setAlertaActiva(true);
                    sensor.setNivelAlerta(NivelAlerta.CRITICA);
                    sensor.setMensajeAlerta("🔴 FLUJO CRÍTICO: < 25 L/min - Obstrucción grave");
                } else if (sensor.getValor() < 35.0) {
                    // ADVERTENCIA
                    sensor.setAlertaActiva(true);
                    sensor.setNivelAlerta(NivelAlerta.ADVERTENCIA);
                    sensor.setMensajeAlerta("🟡 Flujo bajo - Verificar");
                }
                break;

            case CONSUMO_ENERGIA:
                if (sensor.getValor() > 14.0) {
                    // ADVERTENCIA (no crítica)
                    sensor.setAlertaActiva(true);
                    sensor.setNivelAlerta(NivelAlerta.ADVERTENCIA);
                    sensor.setMensajeAlerta("🟡 Consumo energético elevado");
                }
                break;
        }
    }

    @Transactional
    private void detenerServicioAutomaticamente(Bahia bahia, SensorTelemetria sensor) {
        log.error("🚨🚨🚨 ALERTA CRÍTICA - DETENIENDO SERVICIO 🚨🚨🚨");
        log.error("Bahía: {} | Sensor: {} | Valor: {} {}",
                bahia.getNumero(),
                sensor.getTipoSensor(),
                sensor.getValor(),
                sensor.getUnidad());

        // 1. Cambiar bahía a MANTENIMIENTO
        bahia.setEstado(EstadoBahia.MANTENIMIENTO);
        bahia.setDisponible(false);
        bahiaRepository.save(bahia);

        // 2. Buscar la reserva EN_PROGRESO en esa bahía (solo la actual)
        List<Reserva> reservasActivas = reservaRepository.findByBahiaAndEstado(
                bahia, EstadoReserva.EN_PROGRESO);

        for (Reserva reserva : reservasActivas) {
            reserva.setEstado(EstadoReserva.EN_ESPERA);
            reserva.setObservaciones(
                    (reserva.getObservaciones() != null ? reserva.getObservaciones() + " | " : "") +
                            "⚠️ SERVICIO DETENIDO POR ALERTA: " + sensor.getMensajeAlerta());
            reservaRepository.save(reserva);

            log.warn("⏸️ Reserva #{} puesta EN_ESPERA", reserva.getId());
        }

        log.error("🛑 SERVICIO DETENIDO - Requiere intervención de OPERADOR");
        log.error("⏸️ TELEMETRÍA SUSPENDIDA - No se generarán más datos hasta resolver la alerta");
    }
}