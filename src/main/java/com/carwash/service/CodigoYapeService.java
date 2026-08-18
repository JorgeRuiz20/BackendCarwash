package com.carwash.service;

import com.carwash.exception.BusinessException;
import com.carwash.model.CodigoYape;
import com.carwash.model.Usuario;
import com.carwash.repository.CodigoYapeRepository;
import com.carwash.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CodigoYapeService {
    private final CodigoYapeRepository codigoYapeRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public boolean validarYUsarCodigo(Long usuarioId, String codigo, String telefono) {
        // Verificar que el código tenga máximo 5 dígitos
        if (codigo.length() > 5 || !codigo.matches("\\d+")) {
            throw new BusinessException("Código inválido. Debe tener máximo 5 dígitos");
        }

        // Verificar si el usuario ya usó este código
        boolean yaUsado = codigoYapeRepository.existsByUsuarioIdAndCodigoAndUsadoTrue(
                usuarioId, codigo);

        if (yaUsado) {
            throw new BusinessException("Ya has usado este código anteriormente");
        }

        // Obtener el usuario
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        // Registrar el uso del código
        CodigoYape nuevoUso = CodigoYape.builder()
                .usuario(usuario)
                .codigo(codigo)
                .numeroTelefono(telefono)
                .fechaUso(LocalDateTime.now())
                .usado(true)
                .build();

        codigoYapeRepository.save(nuevoUso);
        return true;
    }
}
