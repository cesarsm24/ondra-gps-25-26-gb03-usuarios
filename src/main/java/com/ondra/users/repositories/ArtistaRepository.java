package com.ondra.users.repositories;

import com.ondra.users.models.dao.Artista;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de entidades {@link Artista}.
 *
 * <p>Incluye operaciones de consulta por usuario, verificación de identificadores
 * únicos, búsqueda por slug y obtención de artistas en tendencia.</p>
 */
public interface ArtistaRepository extends JpaRepository<Artista, Long> {

    /**
     * Busca un artista por el identificador de su usuario asociado.
     *
     * @param idUsuario identificador del usuario
     * @return artista correspondiente si existe
     */
    Optional<Artista> findByUsuario_IdUsuario(Long idUsuario);

    /**
     * Obtiene los artistas marcados como tendencia ordenados por fecha descendente.
     *
     * @return lista de artistas en tendencia
     */
    List<Artista> findByEsTendenciaTrueOrderByFechaInicioArtisticoDesc();

    /**
     * Busca un artista por su identificador de URL.
     *
     * @param slugArtistico slug del artista
     * @return artista correspondiente si existe
     */
    Optional<Artista> findBySlugArtistico(String slugArtistico);

    /**
     * Comprueba si existe un artista con el slug indicado.
     *
     * @param slugArtistico slug del artista
     * @return true si existe, false en caso contrario
     */
    boolean existsBySlugArtistico(String slugArtistico);

    /**
     * Busca un artista por su nombre artístico.
     *
     * @param nombreArtistico nombre artístico del artista
     * @return artista correspondiente si existe
     */
    Optional<Artista> findByNombreArtistico(String nombreArtistico);

    /**
     * Busca artistas aplicando filtros opcionales y paginación.
     *
     * @param search término de búsqueda por nombre artístico
     * @param esTendencia indicador de tendencia
     * @param pageable configuración de paginación
     * @return página de resultados coincidentes
     */
    @Query("""
           SELECT a FROM Artista a
           WHERE (LOWER(a.nombreArtistico) LIKE LOWER(CONCAT('%', :search, '%')) OR :search IS NULL)
             AND (:esTendencia IS NULL OR a.esTendencia = :esTendencia)
             AND a.usuario.activo = true
             AND a.usuario.tipoUsuario = 'ARTISTA'
           """)
    Page<Artista> buscarArtistas(
            @Param("search") String search,
            @Param("esTendencia") Boolean esTendencia,
            Pageable pageable
    );
}
