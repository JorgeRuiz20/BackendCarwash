package com.carwash.repository;

import com.carwash.model.Cupon;
import com.carwash.model.TipoOrigenCupon;
import com.carwash.model.PerfilCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CuponRepository extends JpaRepository<Cupon, Long> {
    
    // ========== MÉTODOS EXISTENTES ==========
    Optional<Cupon> findByCodigo(String codigo);

    List<Cupon> findByActivoTrue();

    @Query("SELECT c FROM Cupon c WHERE c.codigo = :codigo AND c.activo = true " +
           "AND c.fechaInicio <= :fecha AND c.fechaExpiracion >= :fecha " +
           "AND (c.usosMaximos IS NULL OR c.usosActuales < c.usosMaximos)")
    Optional<Cupon> findCuponValido(
            @Param("codigo") String codigo,
            @Param("fecha") LocalDateTime fecha);
    
    // ========== MÉTODOS PARA FIDELIZACIÓN - CORREGIDOS ==========
    
    /**
     * Verifica si existe un código (para evitar duplicados)
     */
    boolean existsByCodigo(String codigo);
    
    /**
     * ✅ CORRECCIÓN: Cambiado de "cliente" (Usuario) a "perfilCliente" (PerfilCliente)
     * Obtiene todos los cupones de fidelización de un cliente
     */
    @Query("SELECT c FROM Cupon c WHERE c.perfilCliente = :perfil " +
           "AND c.tipoOrigen = 'FIDELIZACION' " +
           "ORDER BY c.fechaInicio DESC")
    List<Cupon> findCuponesFidelizacionPerfil(@Param("perfil") PerfilCliente perfil);
    
    /**
     * ✅ CORRECCIÓN: Cambiado de "cliente" a "perfilCliente"
     * Obtiene cupones de fidelización activos de un cliente
     */
    @Query("SELECT c FROM Cupon c WHERE c.perfilCliente = :perfil " +
           "AND c.tipoOrigen = 'FIDELIZACION' " +
           "AND c.activo = true " +
           "AND c.fechaInicio <= :fechaActual " +
           "AND c.fechaExpiracion > :fechaActual " +
           "AND (c.usosMaximos IS NULL OR c.usosActuales < c.usosMaximos)")
    List<Cupon> findCuponesFidelizacionActivosPerfil(
            @Param("perfil") PerfilCliente perfil,
            @Param("fechaActual") LocalDateTime fechaActual
    );
    
    /**
     * Obtiene cupones promocionales activos (para cualquier cliente)
     */
    @Query("SELECT c FROM Cupon c WHERE c.tipoOrigen = 'PROMOCIONAL' " +
           "AND c.activo = true " +
           "AND c.fechaInicio <= :fechaActual " +
           "AND c.fechaExpiracion > :fechaActual " +
           "AND (c.usosMaximos IS NULL OR c.usosActuales < c.usosMaximos)")
    List<Cupon> findCuponesPromocionalesActivos(@Param("fechaActual") LocalDateTime fechaActual);
    
    /**
     * Obtiene cupones expirados que aún están marcados como activos
     */
    @Query("SELECT c FROM Cupon c WHERE c.activo = true " +
           "AND c.fechaExpiracion < :fechaActual")
    List<Cupon> findCuponesExpirados(@Param("fechaActual") LocalDateTime fechaActual);
    
    /**
     * ✅ CORRECCIÓN: Cambiado de "cliente" a "perfilCliente"
     * Obtiene cupones usados de un cliente
     */
    @Query("SELECT c FROM Cupon c WHERE c.perfilCliente = :perfil " +
           "AND c.tipoOrigen = 'FIDELIZACION' " +
           "AND (c.activo = false OR c.usosActuales >= c.usosMaximos)")
    List<Cupon> findCuponesFidelizacionUsadosPerfil(@Param("perfil") PerfilCliente perfil);
    
    /**
     * ✅ CORRECCIÓN: Cambiado de "cliente" a "perfilCliente"
     * Cuenta cupones de fidelización activos de un cliente
     */
    @Query("SELECT COUNT(c) FROM Cupon c WHERE c.perfilCliente = :perfil " +
           "AND c.tipoOrigen = 'FIDELIZACION' " +
           "AND c.activo = true " +
           "AND c.fechaInicio <= :fechaActual " +
           "AND c.fechaExpiracion > :fechaActual " +
           "AND (c.usosMaximos IS NULL OR c.usosActuales < c.usosMaximos)")
    Long contarCuponesFidelizacionActivosPerfil(
            @Param("perfil") PerfilCliente perfil,
            @Param("fechaActual") LocalDateTime fechaActual
    );
    
    /**
     * Busca cupones por tipo de origen
     */
    List<Cupon> findByTipoOrigen(TipoOrigenCupon tipoOrigen);
}