package com.ondra.users.repositories;

import com.ondra.users.models.dao.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link Usuario}.
 *
 * <p>Proporciona métodos para acceder a los datos de los usuarios en la base de datos,
 * incluyendo consultas personalizadas por email, Google UID y usuarios inactivos.</p>
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su email.
     *
     * @param emailUsuario Email del usuario
     * @return {@link Optional} que contiene el usuario si existe
     */
    Optional<Usuario> findByEmailUsuario(String emailUsuario);

    /**
     * Busca un usuario por su UID de Google.
     *
     * @param googleUid UID de Google del usuario
     * @return {@link Optional} que contiene el usuario si existe
     */
    Optional<Usuario> findByGoogleUid(String googleUid);

    /**
     * Lista todos los usuarios inactivos cuya fecha de registro es anterior a la fecha indicada.
     *
     * @param fecha Fecha límite de registro
     * @return lista de usuarios inactivos
     */
    List<Usuario> findByActivoFalseAndFechaRegistroBefore(LocalDateTime fecha);

    Optional<Usuario> findByTokenVerificacion(String tokenVerificacion);

    Optional<Usuario> findByCodigoRecuperacion(String codigoRecuperacion);

    List<Usuario> findByActivoTrueAndEmailVerificadoFalseAndFechaRegistroBefore(LocalDateTime fecha);

    List<Usuario> findByTokenVerificacionIsNotNullAndFechaExpiracionTokenBefore(LocalDateTime fecha);

}