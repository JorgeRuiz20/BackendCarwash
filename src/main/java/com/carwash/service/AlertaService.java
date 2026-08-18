package com.carwash.service;

import com.carwash.dto.SensorTelemetriaDTO;
import com.carwash.model.*;
import com.carwash.repository.*;
import com.carwash.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertaService {
    private final SensorTelemetriaRepository sensorRepository;
    private final BahiaRepository bahiaRepository;
    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper;

    public List<SensorTelemetriaDTO> obtenerAlertasActivas() {
        return sensorRepository.findByAlertaActivaTrueAndAlertaResueltaFalse()
                .stream()
                .map(s -> modelMapper.map(s, SensorTelemetriaDTO.class))
                .collect(Collectors.toList());
    }

    public List<SensorTelemetriaDTO> obtenerAlertasCriticas() {
        return sensorRepository.findByNivelAlertaAndAlertaResueltaFalse(NivelAlerta.CRITICA)
                .stream()
                .map(s -> modelMapper.map(s, SensorTelemetriaDTO.class))
                .collect(Collectors.toList());
    }

@Transactional
public SensorTelemetriaDTO resolverAlerta(Long alertaId, String operadorEmail,
        String comentario, boolean reanudarServicio) {
    
    SensorTelemetria sensor = sensorRepository.findById(alertaId)
            .orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada"));

    // ✅ FIX: buscar por email en vez de por ID
    Usuario operador = usuarioRepository.findByEmail(operadorEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Operador no encontrado"));

    // ... resto igual


        // Marcar alerta como resuelta
        sensor.setAlertaResuelta(true);
        sensor.setFechaResolucion(LocalDateTime.now());
        sensor.setOperadorResuelve(operador);
        sensor.setComentarioResolucion(comentario);
        sensorRepository.save(sensor);

        // Si decide reanudar el servicio
        if (reanudarServicio) {
            Bahia bahia = sensor.getBahia();
            bahia.setEstado(EstadoBahia.EN_USO);
            bahia.setDisponible(true);
            bahiaRepository.save(bahia);

            // Reanudar reservas en espera
            List<Reserva> reservasEnEspera = reservaRepository.findByBahiaAndEstado(
                    bahia, EstadoReserva.EN_ESPERA);

            for (Reserva reserva : reservasEnEspera) {
                reserva.setEstado(EstadoReserva.EN_PROGRESO);
                reserva.setObservaciones(
                        (reserva.getObservaciones() != null ? reserva.getObservaciones() + " | " : "") +
                                "✅ Servicio reanudado por operador: " + operador.getNombre());
                reservaRepository.save(reserva);
            }
        }

        return modelMapper.map(sensor, SensorTelemetriaDTO.class);
    }
}
