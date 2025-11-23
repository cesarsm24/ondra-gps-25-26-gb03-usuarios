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
 * Repositorio para la gestión de tokens de refresco.
 *
 * <p>Proporciona operaciones de acceso a datos para tokens de autenticación,
 * incluyendo consultas por usuario y limpieza de tokens expirados.</p>
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Busca un token de refresco por su valor.
     *
     * @param token valor del token
     * @return Optional con el token si existe
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Busca todos los tokens de refresco de un usuario.
     *
     * @param idUsuario identificador del usuario
     * @return lista de tokens del usuario
     */
    List<RefreshToken> findByUsuario_IdUsuario(Long idUsuario);

    /**
     * Elimina todos los tokens de refresco expirados.
     *
     * @param now fecha y hora actual para comparación
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.fechaExpiracion < :now")
    void eliminarTokensExpirados(LocalDateTime now);

    /**
     * Elimina todos los tokens de refresco de un usuario.
     *
     * @param idUsuario identificador del usuario
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.usuario.idUsuario = :idUsuario")
    void eliminarTodosPorUsuario(Long idUsuario);
}