package com.ondra.users.repositories;

import com.ondra.users.models.dao.Usuario;
import com.ondra.users.models.enums.TipoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de usuarios.
 *
 * <p>Proporciona operaciones de acceso a datos para usuarios,
 * incluyendo consultas por email, autenticación externa y gestión de cuentas inactivas.</p>
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su email.
     *
     * @param emailUsuario email del usuario
     * @return Optional con el usuario si existe
     */
    Optional<Usuario> findByEmailUsuario(String emailUsuario);

    /**
     * Busca un usuario por su identificador de Google.
     *
     * @param googleUid identificador de Google del usuario
     * @return Optional con el usuario si existe
     */
    Optional<Usuario> findByGoogleUid(String googleUid);

    /**
     * Busca usuarios inactivos registrados antes de una fecha.
     *
     * @param fecha fecha límite de registro
     * @return lista de usuarios inactivos
     */
    List<Usuario> findByActivoFalseAndFechaRegistroBefore(LocalDateTime fecha);

    /**
     * Busca un usuario por su token de verificación.
     *
     * @param tokenVerificacion token de verificación
     * @return Optional con el usuario si existe
     */
    Optional<Usuario> findByTokenVerificacion(String tokenVerificacion);

    /**
     * Busca un usuario por su código de recuperación.
     *
     * @param codigoRecuperacion código de recuperación
     * @return Optional con el usuario si existe
     */
    Optional<Usuario> findByCodigoRecuperacion(String codigoRecuperacion);

    /**
     * Busca usuarios activos sin verificar registrados antes de una fecha.
     *
     * @param fecha fecha límite de registro
     * @return lista de usuarios sin verificar
     */
    List<Usuario> findByActivoTrueAndEmailVerificadoFalseAndFechaRegistroBefore(LocalDateTime fecha);

    /**
     * Busca usuarios con token de verificación expirado.
     *
     * @param fecha fecha límite de expiración
     * @return lista de usuarios con token expirado
     */
    List<Usuario> findByTokenVerificacionIsNotNullAndFechaExpiracionTokenBefore(LocalDateTime fecha);

    /**
     * Busca un usuario por su slug.
     *
     * @param slug slug del usuario
     * @return Optional con el usuario si existe
     */
    Optional<Usuario> findBySlug(String slug);

    /**
     * Verifica si existe un usuario con un slug.
     *
     * @param slug slug a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsBySlug(String slug);

    /**
     * Cuenta usuarios activos de un tipo específico.
     *
     * @param tipoUsuario tipo de usuario
     * @return número de usuarios activos del tipo
     */
    long countByTipoUsuarioAndActivoTrue(TipoUsuario tipoUsuario);
}