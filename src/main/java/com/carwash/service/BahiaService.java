package com.carwash.service;

import com.carwash.dto.BahiaDTO;
import com.carwash.model.Bahia;
import com.carwash.model.EstadoBahia;
import com.carwash.repository.BahiaRepository;
import com.carwash.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BahiaService {
    private final BahiaRepository bahiaRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public BahiaDTO crearBahia(BahiaDTO bahiaDTO) {
        Bahia bahia = modelMapper.map(bahiaDTO, Bahia.class);
        bahia.setEstado(EstadoBahia.DISPONIBLE);
        bahia.setDisponible(true);
        bahia = bahiaRepository.save(bahia);
        return modelMapper.map(bahia, BahiaDTO.class);
    }

    public BahiaDTO obtenerPorId(Long id) {
        Bahia bahia = bahiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bahía no encontrada"));
        return modelMapper.map(bahia, BahiaDTO.class);
    }

    public List<BahiaDTO> listarTodas() {
        return bahiaRepository.findAll().stream()
                .map(b -> modelMapper.map(b, BahiaDTO.class))
                .collect(Collectors.toList());
    }

    public List<BahiaDTO> listarDisponibles() {
        return bahiaRepository.findBahiasDisponibles().stream()
                .map(b -> modelMapper.map(b, BahiaDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public BahiaDTO actualizarEstado(Long id, EstadoBahia nuevoEstado) {
        Bahia bahia = bahiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bahía no encontrada"));

        bahia.setEstado(nuevoEstado);
        bahia.setDisponible(nuevoEstado == EstadoBahia.DISPONIBLE);

        bahia = bahiaRepository.save(bahia);
        return modelMapper.map(bahia, BahiaDTO.class);
    }

    public Long contarDisponibles() {
        return bahiaRepository.countBahiasDisponibles();
    }
}
