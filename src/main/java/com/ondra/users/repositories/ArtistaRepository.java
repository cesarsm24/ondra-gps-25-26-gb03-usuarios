package com.ondra.users.repositories;

import com.ondra.users.models.dao.Artista;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad {@link Artista}.
 *
 * <p>Proporciona operaciones de acceso a datos de artistas incluyendo búsquedas
 * por usuario, consultas de artistas en tendencia y validaciones de identificadores únicos.</p>
 */
public interface ArtistaRepository extends JpaRepository<Artista, Long> {

    /**
     * Busca el artista asociado a un usuario específico.
     *
     * @param idUsuario identificador del usuario
     * @return Optional que contiene el artista si existe
     */
    Optional<Artista> findByUsuario_IdUsuario(Long idUsuario);

    /**
     * Obtiene todos los artistas marcados como tendencia ordenados por fecha de inicio descendente.
     *
     * @return lista de artistas en tendencia
     */
    List<Artista> findByEsTendenciaTrueOrderByFechaInicioArtisticoDesc();

    /**
     * Busca un artista por su identificador de URL amigable.
     *
     * @param slugArtistico identificador de URL del artista
     * @return Optional que contiene el artista si existe
     */
    Optional<Artista> findBySlugArtistico(String slugArtistico);

    /**
     * Verifica si existe un artista con el identificador de URL especificado.
     *
     * @param slugArtistico identificador de URL del artista
     * @return true si existe, false en caso contrario
     */
    boolean existsBySlugArtistico(String slugArtistico);

    /**
     * Busca un artista por su nombre artístico.
     *
     * @param nombreArtistico nombre artístico del artista
     * @return Optional que contiene el artista si existe
     */
    Optional<Artista> findByNombreArtistico(String nombreArtistico);
}