package com.ondra.users.services;

import com.ondra.users.dto.*;
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
 * Servicio para la gestión de redes sociales de los artistas.
 *
 * <p>Este servicio permite:</p>
 * <ul>
 *     <li>Listar todas las redes sociales de un artista</li>
 *     <li>Crear nuevas redes sociales</li>
 *     <li>Editar redes sociales existentes</li>
 *     <li>Eliminar redes sociales</li>
 * </ul>
 *
 * <p><strong>Reglas de negocio:</strong></p>
 * <ul>
 *     <li>Solo el propietario del perfil puede modificar sus redes sociales</li>
 *     <li>No se permiten redes sociales duplicadas del mismo tipo (excepto tipo "OTRA")</li>
 *     <li>Las URLs se limpian de espacios en blanco</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedSocialService {

    private final RedSocialRepository redSocialRepository;
    private final ArtistaRepository artistaRepository;

    /**
     * Lista todas las redes sociales de un artista dado su ID.
     * Endpoint público - no requiere autenticación.
     *
     * @param idArtista ID del artista
     * @return Lista de DTOs de redes sociales
     * @throws ArtistaNotFoundException Si el artista no existe
     */
    @Transactional(readOnly = true)
    public List<RedSocialDTO> listarRedesSociales(Long idArtista) {
        // Verificar que el artista existe
        if (!artistaRepository.existsById(idArtista)) {
            log.warn("Intento de listar redes sociales de artista no existente ID: {}", idArtista);
            throw new ArtistaNotFoundException(idArtista);
        }

        List<RedSocial> redesSociales = redSocialRepository.findByArtista_IdArtista(idArtista);

        log.debug("Se encontraron {} redes sociales para artista ID: {}",
                redesSociales.size(), idArtista);

        return redesSociales.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Crea una nueva red social para un artista.
     *
     * @param idArtista ID del artista
     * @param crearDTO DTO con los datos de la red social
     * @param authenticatedUserId ID del usuario autenticado
     * @return DTO de la red social creada
     * @throws ArtistaNotFoundException Si el artista no existe
     * @throws ForbiddenAccessException Si el usuario no tiene permisos
     * @throws InvalidDataException Si el tipo de red social no es válido
     * @throws DuplicateSocialNetworkException Si ya existe una red social del mismo tipo
     */
    @Transactional
    public RedSocialDTO crearRedSocial(
            Long idArtista,
            RedSocialCrearDTO crearDTO,
            Long authenticatedUserId
    ) {
        // Buscar artista
        Artista artista = artistaRepository.findById(idArtista)
                .orElseThrow(() -> {
                    log.warn("Intento de crear red social para artista no existente ID: {}", idArtista);
                    return new ArtistaNotFoundException(idArtista);
                });

        // Validar que el usuario autenticado es el dueño del perfil
        if (!artista.getUsuario().getIdUsuario().equals(authenticatedUserId)) {
            log.warn("Usuario ID: {} intentó añadir red social a perfil de artista ID: {} sin permisos",
                    authenticatedUserId, idArtista);
            throw new ForbiddenAccessException(
                    "No tienes permiso para añadir redes sociales a este perfil"
            );
        }

        // Validar tipo de red social
        TipoRedSocial tipoRedSocial;
        try {
            tipoRedSocial = TipoRedSocial.valueOf(crearDTO.getTipoRedSocial().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Tipo de red social inválido: {}", crearDTO.getTipoRedSocial());
            throw new InvalidDataException(
                    "Tipo de red social no válido: " + crearDTO.getTipoRedSocial()
            );
        }

        // Validar que no exista ya una red social del mismo tipo (excepto OTRA)
        if (tipoRedSocial != TipoRedSocial.OTRA) {
            boolean existeRedSocial = redSocialRepository
                    .existsByArtista_IdArtistaAndTipoRedSocial(idArtista, tipoRedSocial);

            if (existeRedSocial) {
                log.warn("Artista ID: {} ya tiene una red social del tipo: {}",
                        idArtista, tipoRedSocial);
                throw new DuplicateSocialNetworkException(crearDTO.getTipoRedSocial());
            }
        }

        // Crear red social
        RedSocial redSocial = RedSocial.builder()
                .artista(artista)
                .tipoRedSocial(tipoRedSocial)
                .urlRedSocial(crearDTO.getUrlRedSocial().trim())
                .build();

        RedSocial redSocialGuardada = redSocialRepository.save(redSocial);

        log.info("Red social creada: ID: {} - Tipo: {} para artista ID: {}",
                redSocialGuardada.getIdRedSocial(), tipoRedSocial, idArtista);

        return convertirADTO(redSocialGuardada);
    }

    /**
     * Edita los datos de una red social específica.
     *
     * @param idArtista ID del artista
     * @param idRed ID de la red social
     * @param editarDTO DTO con los campos a actualizar
     * @param authenticatedUserId ID del usuario autenticado
     * @return DTO de la red social actualizada
     * @throws ArtistaNotFoundException Si el artista no existe
     * @throws RedSocialNotFoundException Si la red social no existe
     * @throws ForbiddenAccessException Si el usuario no tiene permisos
     * @throws SocialNetworkMismatchException Si la red social no pertenece al artista
     * @throws InvalidDataException Si el tipo de red social es inválido
     * @throws DuplicateSocialNetworkException Si ya existe otra red social del mismo tipo
     */
    @Transactional
    public RedSocialDTO editarRedSocial(
            Long idArtista,
            Long idRed,
            RedSocialEditarDTO editarDTO,
            Long authenticatedUserId
    ) {
        // Buscar artista
        Artista artista = artistaRepository.findById(idArtista)
                .orElseThrow(() -> {
                    log.warn("Intento de editar red social para artista no existente ID: {}", idArtista);
                    return new ArtistaNotFoundException(idArtista);
                });

        // Validar permisos
        if (!artista.getUsuario().getIdUsuario().equals(authenticatedUserId)) {
            log.warn("Usuario ID: {} intentó modificar red social de artista ID: {} sin permisos",
                    authenticatedUserId, idArtista);
            throw new ForbiddenAccessException(
                    "No tienes permiso para modificar redes sociales de este perfil"
            );
        }

        // Buscar red social
        RedSocial redSocial = redSocialRepository.findById(idRed)
                .orElseThrow(() -> {
                    log.warn("Red social no encontrada ID: {}", idRed);
                    return new RedSocialNotFoundException(idRed);
                });

        // Validar que la red social pertenece al artista
        if (!redSocial.getArtista().getIdArtista().equals(idArtista)) {
            log.warn("Red social ID: {} no pertenece al artista ID: {}", idRed, idArtista);
            throw new SocialNetworkMismatchException();
        }

        // Actualizar tipo de red social si se proporciona
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

            // Validar que no exista otra red social del mismo tipo (excepto OTRA)
            if (nuevoTipo != TipoRedSocial.OTRA) {
                boolean existeOtraRed = redSocialRepository
                        .existsByArtista_IdArtistaAndTipoRedSocialAndIdRedSocialNot(
                                idArtista, nuevoTipo, idRed);

                if (existeOtraRed) {
                    log.warn("Artista ID: {} ya tiene otra red social del tipo: {}",
                            idArtista, nuevoTipo);
                    throw new DuplicateSocialNetworkException(editarDTO.getTipoRedSocial());
                }
            }

            redSocial.setTipoRedSocial(nuevoTipo);
        }

        // Actualizar URL si se proporciona
        if (editarDTO.getUrlRedSocial() != null && !editarDTO.getUrlRedSocial().isBlank()) {
            redSocial.setUrlRedSocial(editarDTO.getUrlRedSocial().trim());
        }

        RedSocial redSocialActualizada = redSocialRepository.save(redSocial);

        log.info("Red social actualizada: ID: {} para artista ID: {}", idRed, idArtista);

        return convertirADTO(redSocialActualizada);
    }

    /**
     * Elimina una red social del perfil de un artista.
     *
     * @param idArtista ID del artista
     * @param idRed ID de la red social
     * @param authenticatedUserId ID del usuario autenticado
     * @throws ArtistaNotFoundException Si el artista no existe
     * @throws RedSocialNotFoundException Si la red social no existe
     * @throws ForbiddenAccessException Si el usuario no tiene permisos
     * @throws SocialNetworkMismatchException Si la red social no pertenece al artista
     */
    @Transactional
    public void eliminarRedSocial(
            Long idArtista,
            Long idRed,
            Long authenticatedUserId
    ) {
        // Buscar artista
        Artista artista = artistaRepository.findById(idArtista)
                .orElseThrow(() -> {
                    log.warn("Intento de eliminar red social para artista no existente ID: {}", idArtista);
                    return new ArtistaNotFoundException(idArtista);
                });

        // Validar permisos
        if (!artista.getUsuario().getIdUsuario().equals(authenticatedUserId)) {
            log.warn("Usuario ID: {} intentó eliminar red social de artista ID: {} sin permisos",
                    authenticatedUserId, idArtista);
            throw new ForbiddenAccessException(
                    "No tienes permiso para eliminar redes sociales de este perfil"
            );
        }

        // Buscar red social
        RedSocial redSocial = redSocialRepository.findById(idRed)
                .orElseThrow(() -> {
                    log.warn("Red social no encontrada ID: {}", idRed);
                    return new RedSocialNotFoundException(idRed);
                });

        // Validar que la red social pertenece al artista
        if (!redSocial.getArtista().getIdArtista().equals(idArtista)) {
            log.warn("Red social ID: {} no pertenece al artista ID: {}", idRed, idArtista);
            throw new SocialNetworkMismatchException();
        }

        // Eliminar
        redSocialRepository.delete(redSocial);

        log.info("Red social eliminada: ID: {} del artista ID: {}", idRed, idArtista);
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Convierte una entidad RedSocial a DTO.
     *
     * @param redSocial Entidad RedSocial
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