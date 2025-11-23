package com.ondra.users.repositories;

import com.ondra.users.models.dao.Seguimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de seguimientos entre usuarios.
 *
 * <p>Proporciona operaciones de acceso a datos para relaciones de seguimiento,
 * incluyendo consultas bidireccionales y conteo de seguidores.</p>
 */
@Repository
public interface SeguimientoRepository extends JpaRepository<Seguimiento, Long> {

    /**
     * Busca una relación de seguimiento entre dos usuarios.
     *
     * @param idSeguidor identificador del usuario seguidor
     * @param idSeguido identificador del usuario seguido
     * @return Optional con el seguimiento si existe
     */
    Optional<Seguimiento> findBySeguidorIdUsuarioAndSeguidoIdUsuario(Long idSeguidor, Long idSeguido);

    /**
     * Verifica si existe una relación de seguimiento entre dos usuarios.
     *
     * @param idSeguidor identificador del usuario seguidor
     * @param idSeguido identificador del usuario seguido
     * @return true si existe, false en caso contrario
     */
    boolean existsBySeguidorIdUsuarioAndSeguidoIdUsuario(Long idSeguidor, Long idSeguido);

    /**
     * Busca todos los usuarios seguidos por un usuario.
     *
     * @param idSeguidor identificador del usuario seguidor
     * @return lista de seguimientos del usuario
     */
    List<Seguimiento> findBySeguidorIdUsuario(Long idSeguidor);

    /**
     * Busca todos los seguidores de un usuario.
     *
     * @param idSeguido identificador del usuario seguido
     * @return lista de seguidores del usuario
     */
    List<Seguimiento> findBySeguidoIdUsuario(Long idSeguido);

    /**
     * Cuenta el número de usuarios seguidos por un usuario.
     *
     * @param idSeguidor identificador del usuario seguidor
     * @return número de usuarios seguidos
     */
    long countBySeguidorIdUsuario(Long idSeguidor);

    /**
     * Cuenta el número de seguidores de un usuario.
     *
     * @param idSeguido identificador del usuario seguido
     * @return número de seguidores
     */
    long countBySeguidoIdUsuario(Long idSeguido);

    /**
     * Elimina todos los seguimientos asociados a un usuario.
     *
     * @param idUsuario1 identificador del usuario como seguidor
     * @param idUsuario2 identificador del usuario como seguido
     */
    @Modifying
    @Query("DELETE FROM Seguimiento s WHERE s.seguidor.idUsuario = :idUsuario1 OR s.seguido.idUsuario = :idUsuario2")
    void deleteBySeguidorIdUsuarioOrSeguidoIdUsuario(@Param("idUsuario1") Long idUsuario1, @Param("idUsuario2") Long idUsuario2);

    /**
     * Elimina una relación de seguimiento específica.
     *
     * @param idSeguidor identificador del usuario seguidor
     * @param idSeguido identificador del usuario seguido
     */
    @Modifying
    @Query("DELETE FROM Seguimiento s WHERE s.seguidor.idUsuario = :idSeguidor AND s.seguido.idUsuario = :idSeguido")
    void deleteBySeguidorIdUsuarioAndSeguidoIdUsuario(@Param("idSeguidor") Long idSeguidor, @Param("idSeguido") Long idSeguido);

    /**
     * Verifica si existe una relación de seguimiento entre dos usuarios.
     *
     * @param idSeguidor identificador del usuario seguidor
     * @param idSeguido identificador del usuario seguido
     * @return true si existe, false en caso contrario
     */
    boolean existsBySeguidor_IdUsuarioAndSeguido_IdUsuario(Long idSeguidor, Long idSeguido);
}