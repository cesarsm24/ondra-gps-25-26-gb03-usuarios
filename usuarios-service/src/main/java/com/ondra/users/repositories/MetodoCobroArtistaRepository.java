package com.ondra.users.repositories;

import com.ondra.users.models.dao.Artista;
import com.ondra.users.models.dao.MetodoCobroArtista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la gestión de métodos de cobro de artistas.
 *
 * <p>Proporciona operaciones de acceso a datos para métodos de cobro,
 * incluyendo consultas filtradas por artista y validación de propiedad.</p>
 */
@Repository
public interface MetodoCobroArtistaRepository extends JpaRepository<MetodoCobroArtista, Long> {

    /**
     * Busca todos los métodos de cobro asociados a un artista.
     *
     * @param artista entidad del artista
     * @return lista de métodos de cobro del artista
     */
    List<MetodoCobroArtista> findByArtista(Artista artista);

    /**
     * Busca un método de cobro específico verificando que pertenezca al artista.
     *
     * @param id identificador del método de cobro
     * @param artista entidad del artista propietario
     * @return Optional con el método de cobro si existe y pertenece al artista
     */
    Optional<MetodoCobroArtista> findByIdMetodoCobroArtistaAndArtista(Long id, Artista artista);

    /**
     * Elimina un método de cobro verificando que pertenezca al artista.
     *
     * @param id identificador del método de cobro
     * @param artista entidad del artista propietario
     */
    void deleteByIdMetodoCobroArtistaAndArtista(Long id, Artista artista);

    /**
     * Busca todos los métodos de cobro de un artista por su identificador.
     *
     * @param idArtista identificador del artista
     * @return lista de métodos de cobro del artista
     */
    List<MetodoCobroArtista> findByArtista_IdArtista(Long idArtista);
}