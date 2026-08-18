package com.carwash.repository;

import com.carwash.model.PerfilCliente;
import com.carwash.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PerfilClienteRepository extends JpaRepository<PerfilCliente, Long> {
    
    // Método existente
    Optional<PerfilCliente> findByUsuarioId(Long usuarioId);

    // Método existente
    @Query("SELECT p FROM PerfilCliente p ORDER BY p.puntosFidelidad DESC")
    List<PerfilCliente> findTopClientesByPuntos();
    
    // ========== NUEVO MÉTODO PARA FIDELIZACIÓN ==========
    
    /**
     * Busca el perfil por el objeto Usuario
     */
    Optional<PerfilCliente> findByUsuario(Usuario usuario);
}