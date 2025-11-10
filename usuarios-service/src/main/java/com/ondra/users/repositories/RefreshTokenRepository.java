package com.ondra.users.repositories;

import com.ondra.users.models.dao.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de Refresh Tokens en la base de datos.
 * Proporciona métodos para buscar, eliminar y listar tokens asociados a usuarios.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Busca un Refresh Token por su valor.
     *
     * @param token Valor del token
     * @return Optional con el Refresh Token si existe
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Obtiene todos los Refresh Tokens asociados a un ID de usuario.
     *
     * @param idUsuario ID del usuario
     * @return Lista de Refresh Tokens del usuario
     */
    List<RefreshToken> findByUsuario_IdUsuario(Long idUsuario);

    /**
     * Elimina todos los Refresh Tokens que han expirado.
     *
     * @param now Fecha y hora actual para comparar expiración
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.fechaExpiracion < :now")
    void eliminarTokensExpirados(LocalDateTime now);

    /**
     * Elimina todos los Refresh Tokens asociados a un ID de usuario.
     *
     * @param idUsuario ID del usuario
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.usuario.idUsuario = :idUsuario")
    void eliminarTodosPorUsuario(Long idUsuario);
}