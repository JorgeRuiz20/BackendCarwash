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
public class SensorTelemetriaService {
    private final SensorTelemetriaRepository sensorRepository;
    private final BahiaRepository bahiaRepository;
    private final ModelMapper modelMapper;
    

    @Transactional
    public SensorTelemetriaDTO registrarLectura(SensorTelemetriaDTO sensorDTO) {
        Bahia bahia = bahiaRepository.findById(sensorDTO.getBahiaId())
                .orElseThrow(() -> new ResourceNotFoundException("Bahía no encontrada"));

        SensorTelemetria sensor = modelMapper.map(sensorDTO, SensorTelemetria.class);
        sensor.setBahia(bahia);
        sensor.setFechaLectura(LocalDateTime.now());

        // Verificar umbrales y generar alertas
        verificarUmbrales(sensor);

        sensor = sensorRepository.save(sensor);
        SensorTelemetriaDTO resultDTO = modelMapper.map(sensor, SensorTelemetriaDTO.class);
        // Mapear el nombre del operador que resuelve
        if (sensor.getOperadorResuelve() != null) {
            resultDTO.setOperadorResuelveName(sensor.getOperadorResuelve().getNombre());
        }
        return resultDTO;
    }

    private void verificarUmbrales(SensorTelemetria sensor) {
        switch (sensor.getTipoSensor()) {
            case NIVEL_AGUA:
                if (sensor.getValor() < 20.0) {
                    sensor.setAlertaActiva(true);
                    sensor.setMensajeAlerta("Nivel de agua bajo");
                }
                break;
            case PRESION_AGUA:
                if (sensor.getValor() < 2.0 || sensor.getValor() > 5.0) {
                    sensor.setAlertaActiva(true);
                    sensor.setMensajeAlerta("Presión de agua fuera de rango");
                }
                break;
            case TEMPERATURA:
                if (sensor.getValor() > 45.0) {
                    sensor.setAlertaActiva(true);
                    sensor.setMensajeAlerta("Temperatura elevada");
                }
                break;
        }
    }

    public List<SensorTelemetriaDTO> obtenerLecturasPorBahia(Long bahiaId) {
        return sensorRepository.findUltimasLecturasByBahia(bahiaId).stream()
                .limit(50)
                .map(s -> modelMapper.map(s, SensorTelemetriaDTO.class))
                .collect(Collectors.toList());
    }

    public List<SensorTelemetriaDTO> obtenerAlertasActivas() {
        return sensorRepository.findByAlertaActivaTrue().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    private SensorTelemetriaDTO convertirADTO(SensorTelemetria sensor) {
        SensorTelemetriaDTO dto = modelMapper.map(sensor, SensorTelemetriaDTO.class);
        if (sensor.getOperadorResuelve() != null) {
            dto.setOperadorResuelveName(sensor.getOperadorResuelve().getNombre());
        }
        return dto;
    }

    public Double obtenerPromedioConsumoAgua(LocalDateTime inicio, LocalDateTime fin) {
        Double promedio = sensorRepository.calcularPromedioSensor(
                TipoSensor.FLUJO_AGUA, inicio, fin);
        return promedio != null ? promedio : 0.0;
    }
}
