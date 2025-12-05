package com.ondra.users.services;

import com.ondra.users.dto.RedSocialCrearDTO;
import com.ondra.users.dto.RedSocialDTO;
import com.ondra.users.dto.RedSocialEditarDTO;
import com.ondra.users.exceptions.*;
import com.ondra.users.models.dao.Artista;
import com.ondra.users.models.dao.RedSocial;
import com.ondra.users.models.enums.TipoRedSocial;
import com.ondra.users.repositories.ArtistaRepository;
import com.ondra.users.repositories.RedSocialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de redes sociales de artistas.
 *
 * <p>Proporciona operaciones CRUD para las redes sociales asociadas a perfiles de artistas,
 * aplicando validaciones de permisos y unicidad por tipo de red social.</p>
 *
 * <p>Reglas de negocio:</p>
 * <ul>
 *   <li>Solo el propietario del perfil puede gestionar sus redes sociales</li>
 *   <li>No se permiten redes sociales duplicadas del mismo tipo por artista</li>
 *   <li>Las URLs se normalizan eliminando espacios en blanco</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedSocialService {

    private final RedSocialRepository redSocialRepository;
    private final ArtistaRepository artistaRepository;

    /**
     * Lista todas las redes sociales de un artista.
     *
     * @param idArtista ID del artista
     * @return Lista de redes sociales del artista
     * @throws ArtistaNotFoundException Si el artista no existe
     */
    @Transactional(readOnly = true)
    public List<RedSocialDTO> listarRedesSociales(Long idArtista) {
        if (!artistaRepository.existsById(idArtista)) {
            log.warn("Intento de listar redes sociales de artista inexistente ID: {}", idArtista);
            throw new ArtistaNotFoundException(idArtista);
        }

        List<RedSocial> redesSociales = redSocialRepository.findByArtista_IdArtista(idArtista);

        log.debug("📋 Encontradas {} redes sociales para artista ID: {}",
                redesSociales.size(), idArtista);

        return redesSociales.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Crea una red social para un artista.
     *
     * @param idArtista ID del artista
     * @param crearDTO Datos de la red social a crear
     * @param authenticatedUserId ID del usuario autenticado
     * @return Red social creada
     * @throws ArtistaNotFoundException Si el artista no existe
     * @throws ForbiddenAccessException Si el usuario no es el propietario del perfil
     * @throws InvalidDataException Si el tipo de red social no es válido
     * @throws DuplicateSocialNetworkException Si ya existe una red del mismo tipo
     */
    @Transactional
    public RedSocialDTO crearRedSocial(
            Long idArtista,
            RedSocialCrearDTO crearDTO,
            Long authenticatedUserId
    ) {
        Artista artista = artistaRepository.findById(idArtista)
                .orElseThrow(() -> {
                    log.warn("Intento de crear red social para artista inexistente ID: {}", idArtista);
                    return new ArtistaNotFoundException(idArtista);
                });

        if (!artista.getUsuario().getIdUsuario().equals(authenticatedUserId)) {
            log.warn("Usuario ID: {} intentó añadir red social a artista ID: {} sin permisos",
                    authenticatedUserId, idArtista);
            throw new ForbiddenAccessException(
                    "No tienes permiso para añadir redes sociales a este perfil"
            );
        }

        TipoRedSocial tipoRedSocial;
        try {
            tipoRedSocial = TipoRedSocial.valueOf(crearDTO.getTipoRedSocial().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Tipo de red social inválido: {}", crearDTO.getTipoRedSocial());
            throw new InvalidDataException(
                    "Tipo de red social no válido: " + crearDTO.getTipoRedSocial()
            );
        }

        boolean existeRedSocial = redSocialRepository
                .existsByArtista_IdArtistaAndTipoRedSocial(idArtista, tipoRedSocial);

        if (existeRedSocial) {
            log.warn("Artista ID: {} ya tiene una red social del tipo: {}", idArtista, tipoRedSocial);
            throw new DuplicateSocialNetworkException(crearDTO.getTipoRedSocial());
        }

        RedSocial redSocial = RedSocial.builder()
                .artista(artista)
                .tipoRedSocial(tipoRedSocial)
                .urlRedSocial(crearDTO.getUrlRedSocial().trim())
                .build();

        RedSocial redSocialGuardada = redSocialRepository.save(redSocial);

        log.info("✅ Red social creada ID: {} - Tipo: {} para artista ID: {}",
                redSocialGuardada.getIdRedSocial(), tipoRedSocial, idArtista);

        return convertirADTO(redSocialGuardada);
    }

    /**
     * Edita una red social existente.
     *
     * @param idArtista ID del artista
     * @param idRed ID de la red social
     * @param editarDTO Datos a actualizar
     * @param authenticatedUserId ID del usuario autenticado
     * @return Red social actualizada
     * @throws ArtistaNotFoundException Si el artista no existe
     * @throws RedSocialNotFoundException Si la red social no existe
     * @throws ForbiddenAccessException Si el usuario no es el propietario
     * @throws SocialNetworkMismatchException Si la red no pertenece al artista
     * @throws InvalidDataException Si el tipo de red social es inválido
     * @throws DuplicateSocialNetworkException Si ya existe otra red del mismo tipo
     */
    @Transactional
    public RedSocialDTO editarRedSocial(
            Long idArtista,
            Long idRed,
            RedSocialEditarDTO editarDTO,
            Long authenticatedUserId
    ) {
        Artista artista = artistaRepository.findById(idArtista)
                .orElseThrow(() -> {
                    log.warn("Intento de editar red social para artista inexistente ID: {}", idArtista);
                    return new ArtistaNotFoundException(idArtista);
                });

        if (!artista.getUsuario().getIdUsuario().equals(authenticatedUserId)) {
            log.warn("Usuario ID: {} intentó modificar red social de artista ID: {} sin permisos",
                    authenticatedUserId, idArtista);
            throw new ForbiddenAccessException(
                    "No tienes permiso para modificar redes sociales de este perfil"
            );
        }

        RedSocial redSocial = redSocialRepository.findById(idRed)
                .orElseThrow(() -> {
                    log.warn("Red social no encontrada ID: {}", idRed);
                    return new RedSocialNotFoundException(idRed);
                });

        if (!redSocial.getArtista().getIdArtista().equals(idArtista)) {
            log.warn("Red social ID: {} no pertenece al artista ID: {}", idRed, idArtista);
            throw new SocialNetworkMismatchException();
        }

        if (editarDTO.getTipoRedSocial() != null && !editarDTO.getTipoRedSocial().isBlank()) {
            TipoRedSocial nuevoTipo;
            try {
                nuevoTipo = TipoRedSocial.valueOf(editarDTO.getTipoRedSocial().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Tipo de red social inválido al editar: {}", editarDTO.getTipoRedSocial());
                throw new InvalidDataException(
                        "Tipo de red social no válido: " + editarDTO.getTipoRedSocial()
                );
            }

            boolean existeOtraRed = redSocialRepository
                    .existsByArtista_IdArtistaAndTipoRedSocialAndIdRedSocialNot(
                            idArtista, nuevoTipo, idRed);

            if (existeOtraRed) {
                log.warn("Artista ID: {} ya tiene otra red social del tipo: {}", idArtista, nuevoTipo);
                throw new DuplicateSocialNetworkException(editarDTO.getTipoRedSocial());
            }

            redSocial.setTipoRedSocial(nuevoTipo);
        }

        if (editarDTO.getUrlRedSocial() != null && !editarDTO.getUrlRedSocial().isBlank()) {
            redSocial.setUrlRedSocial(editarDTO.getUrlRedSocial().trim());
        }

        RedSocial redSocialActualizada = redSocialRepository.save(redSocial);

        log.info("✏️ Red social actualizada ID: {} para artista ID: {}", idRed, idArtista);

        return convertirADTO(redSocialActualizada);
    }

    /**
     * Elimina una red social de un artista.
     *
     * @param idArtista ID del artista
     * @param idRed ID de la red social
     * @param authenticatedUserId ID del usuario autenticado
     * @throws ArtistaNotFoundException Si el artista no existe
     * @throws RedSocialNotFoundException Si la red social no existe
     * @throws ForbiddenAccessException Si el usuario no es el propietario
     * @throws SocialNetworkMismatchException Si la red no pertenece al artista
     */
    @Transactional
    public void eliminarRedSocial(
            Long idArtista,
            Long idRed,
            Long authenticatedUserId
    ) {
        Artista artista = artistaRepository.findById(idArtista)
                .orElseThrow(() -> {
                    log.warn("Intento de eliminar red social para artista inexistente ID: {}", idArtista);
                    return new ArtistaNotFoundException(idArtista);
                });

        if (!artista.getUsuario().getIdUsuario().equals(authenticatedUserId)) {
            log.warn("Usuario ID: {} intentó eliminar red social de artista ID: {} sin permisos",
                    authenticatedUserId, idArtista);
            throw new ForbiddenAccessException(
                    "No tienes permiso para eliminar redes sociales de este perfil"
            );
        }

        RedSocial redSocial = redSocialRepository.findById(idRed)
                .orElseThrow(() -> {
                    log.warn("Red social no encontrada ID: {}", idRed);
                    return new RedSocialNotFoundException(idRed);
                });

        if (!redSocial.getArtista().getIdArtista().equals(idArtista)) {
            log.warn("Red social ID: {} no pertenece al artista ID: {}", idRed, idArtista);
            throw new SocialNetworkMismatchException();
        }

        redSocialRepository.delete(redSocial);

        log.info("🗑️ Red social eliminada ID: {} del artista ID: {}", idRed, idArtista);
    }

    /**
     * Convierte una entidad RedSocial a DTO.
     *
     * @param redSocial Entidad a convertir
     * @return DTO correspondiente
     */
    private RedSocialDTO convertirADTO(RedSocial redSocial) {
        return RedSocialDTO.builder()
                .idRedSocial(redSocial.getIdRedSocial())
                .idArtista(redSocial.getArtista().getIdArtista())
                .tipoRedSocial(redSocial.getTipoRedSocial().name().toLowerCase())
                .urlRedSocial(redSocial.getUrlRedSocial())
                .build();
    }
}