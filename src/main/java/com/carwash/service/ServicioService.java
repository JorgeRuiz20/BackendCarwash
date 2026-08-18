package com.carwash.service;

import com.carwash.dto.ServicioDTO;
import com.carwash.model.Servicio;
import com.carwash.model.TipoServicio;
import com.carwash.repository.ServicioRepository;
import com.carwash.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicioService {
    private final ServicioRepository servicioRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public ServicioDTO crearServicio(ServicioDTO servicioDTO) {
        Servicio servicio = modelMapper.map(servicioDTO, Servicio.class);
        servicio.setActivo(true);
        servicio = servicioRepository.save(servicio);
        return modelMapper.map(servicio, ServicioDTO.class);
    }

    public ServicioDTO obtenerPorId(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));
        return modelMapper.map(servicio, ServicioDTO.class);
    }

    public List<ServicioDTO> listarTodos() {
        return servicioRepository.findAll().stream()
                .map(s -> modelMapper.map(s, ServicioDTO.class))
                .collect(Collectors.toList());
    }

    public List<ServicioDTO> listarActivos() {
        return servicioRepository.findByActivoTrue().stream()
                .map(s -> modelMapper.map(s, ServicioDTO.class))
                .collect(Collectors.toList());
    }

    public List<ServicioDTO> listarPorTipo(TipoServicio tipo) {
        return servicioRepository.findByTipo(tipo).stream()
                .map(s -> modelMapper.map(s, ServicioDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public ServicioDTO actualizarServicio(Long id, ServicioDTO servicioDTO) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        servicio.setNombre(servicioDTO.getNombre());
        servicio.setDescripcion(servicioDTO.getDescripcion());
        servicio.setPrecio(servicioDTO.getPrecio());
        servicio.setDuracionMinutos(servicioDTO.getDuracionMinutos());
        servicio.setTipo(servicioDTO.getTipo());

        servicio = servicioRepository.save(servicio);
        return modelMapper.map(servicio, ServicioDTO.class);
    }

    @Transactional
    public void eliminarServicio(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));
        servicio.setActivo(false);
        servicioRepository.save(servicio);
    }
}
