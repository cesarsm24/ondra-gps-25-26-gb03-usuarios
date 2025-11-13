package com.ondra.users.repositories;

import com.ondra.users.models.dao.RedSocial;
import com.ondra.users.models.enums.TipoRedSocial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad {@link RedSocial}.
 *
 * <p>Proporciona métodos para acceder a las redes sociales asociadas a los artistas
 * en la base de datos, incluyendo consultas personalizadas y verificación de existencia.</p>
 */
@Repository
public interface RedSocialRepository extends JpaRepository<RedSocial, Long> {

    /**
     * Lista todas las redes sociales asociadas a un artista específico.
     *
     * @param idArtista ID del artista
     * @return lista de redes sociales del artista
     */
    List<RedSocial> findByArtista_IdArtista(Long idArtista);

    /**
     * Verifica si existe una red social de un tipo específico asociada a un artista.
     *
     * @param idArtista ID del artista
     * @param tipoRedSocial Tipo de red social
     * @return true si existe, false en caso contrario
     */
    boolean existsByArtista_IdArtistaAndTipoRedSocial(Long idArtista, TipoRedSocial tipoRedSocial);

    /**
     * Verifica si existe una red social de un tipo específico asociada a un artista,
     * excluyendo una red social por su ID.
     *
     * @param idArtista ID del artista
     * @param tipoRedSocial Tipo de red social
     * @param idRedSocial ID de la red social a excluir
     * @return true si existe, false en caso contrario
     */
    boolean existsByArtista_IdArtistaAndTipoRedSocialAndIdRedSocialNot(Long idArtista, TipoRedSocial tipoRedSocial, Long idRedSocial);
}