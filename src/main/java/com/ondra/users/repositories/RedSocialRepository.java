package com.ondra.users.repositories;

import com.ondra.users.models.dao.RedSocial;
import com.ondra.users.models.enums.TipoRedSocial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la gestión de redes sociales de artistas.
 *
 * <p>Proporciona operaciones de acceso a datos para redes sociales,
 * incluyendo consultas por artista y validación de duplicados.</p>
 */
@Repository
public interface RedSocialRepository extends JpaRepository<RedSocial, Long> {

    /**
     * Busca todas las redes sociales de un artista.
     *
     * @param idArtista identificador del artista
     * @return lista de redes sociales del artista
     */
    List<RedSocial> findByArtista_IdArtista(Long idArtista);

    /**
     * Verifica si existe una red social de un tipo para un artista.
     *
     * @param idArtista identificador del artista
     * @param tipoRedSocial tipo de red social
     * @return true si existe, false en caso contrario
     */
    boolean existsByArtista_IdArtistaAndTipoRedSocial(Long idArtista, TipoRedSocial tipoRedSocial);

    /**
     * Verifica si existe una red social de un tipo para un artista, excluyendo un registro.
     *
     * @param idArtista identificador del artista
     * @param tipoRedSocial tipo de red social
     * @param idRedSocial identificador de la red social a excluir
     * @return true si existe, false en caso contrario
     */
    boolean existsByArtista_IdArtistaAndTipoRedSocialAndIdRedSocialNot(Long idArtista, TipoRedSocial tipoRedSocial, Long idRedSocial);
}