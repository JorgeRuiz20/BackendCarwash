package com.carwash.repository;

import com.carwash.model.Usuario;
import com.carwash.model.TipoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    
    // ✅ Asegúrate que este método exista
    Optional<Usuario> findById(Long id);
    
    Boolean existsByEmail(String email);
    List<Usuario> findByTipo(TipoUsuario tipo);
    List<Usuario> findByActivoTrue();
    
    @Query("SELECT u FROM Usuario u WHERE u.tipo = 'CLIENTE' AND u.activo = true")
    List<Usuario> findClientesActivos();
}