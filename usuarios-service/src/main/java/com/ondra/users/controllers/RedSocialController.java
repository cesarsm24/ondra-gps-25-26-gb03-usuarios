package com.ondra.users.controllers;

import com.ondra.users.services.RedSocialService;
import com.ondra.users.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador para gestionar las redes sociales de artistas.
 *
 * <p><strong>Endpoints públicos (no requieren autenticación):</strong></p>
 * <ul>
 *   <li>GET /api/artistas/{id}/redes - Listar redes sociales de un artista</li>
 * </ul>
 *
 * <p><strong>Endpoints protegidos (requieren JWT):</strong></p>
 * <ul>
 *   <li>POST /api/artistas/{id}/redes - Crear red social</li>
 *   <li>PUT /api/artistas/{id}/redes/{id_red} - Editar red social</li>
 *   <li>DELETE /api/artistas/{id}/redes/{id_red} - Eliminar red social</li>
 * </ul>
 *
 * <p><strong>Validaciones de negocio:</strong></p>
 * <ul>
 *   <li>Solo el propietario del perfil puede crear, editar o eliminar sus redes sociales</li>
 *   <li>No se permiten redes sociales duplicadas del mismo tipo (excepto "OTRA")</li>
 *   <li>La red social debe pertenecer al artista especificado</li>
 * </ul>
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class RedSocialController {

    private final RedSocialService redSocialService;

    /**
     * Lista todas las redes sociales registradas de un artista.
     * Endpoint público - no requiere autenticación.
     *
     * @param id Identificador del artista
     * @return Lista de {@link RedSocialDTO} en formato JSON
     */
    @GetMapping(value = "/artistas/{id}/redes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RedSocialDTO>> listarRedesSociales(@PathVariable Long id) {
        List<RedSocialDTO> redesSociales = redSocialService.listarRedesSociales(id);
        return ResponseEntity.ok(redesSociales);
    }

    /**
     * Crea y añade una nueva red social al perfil de un artista.
     * Requiere autenticación JWT.
     *
     * @param id Identificador del artista
     * @param crearDTO Datos de la red social a registrar
     * @param authentication Objeto de autenticación de Spring Security
     * @return Objeto {@link RedSocialDTO} con la red social registrada
     */
    @PostMapping(value = "/artistas/{id}/redes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RedSocialDTO> crearRedSocial(
            @PathVariable Long id,
            @Valid @RequestBody RedSocialCrearDTO crearDTO,
            Authentication authentication) {

        Long authenticatedUserId = Long.parseLong(authentication.getName());

        RedSocialDTO redSocial = redSocialService.crearRedSocial(id, crearDTO, authenticatedUserId);
        return new ResponseEntity<>(redSocial, HttpStatus.CREATED);
    }

    /**
     * Actualiza los datos de una red social de un artista.
     * Requiere autenticación JWT.
     *
     * @param id Identificador del artista
     * @param idRed Identificador de la red social a actualizar
     * @param editarDTO Datos a modificar
     * @param authentication Objeto de autenticación de Spring Security
     * @return Objeto {@link RedSocialDTO} con la información actualizada
     */
    @PutMapping(value = "/artistas/{id}/redes/{id_red}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RedSocialDTO> editarRedSocial(
            @PathVariable Long id,
            @PathVariable("id_red") Long idRed,
            @Valid @RequestBody RedSocialEditarDTO editarDTO,
            Authentication authentication) {

        Long authenticatedUserId = Long.parseLong(authentication.getName());

        RedSocialDTO redSocial = redSocialService.editarRedSocial(id, idRed, editarDTO, authenticatedUserId);
        return ResponseEntity.ok(redSocial);
    }

    /**
     * Elimina una red social del perfil de un artista.
     * Requiere autenticación JWT.
     *
     * @param id Identificador del artista
     * @param idRed Identificador de la red social a eliminar
     * @param authentication Objeto de autenticación de Spring Security
     * @return Objeto {@link SuccessfulResponseDTO} con el resultado de la operación
     */
    @DeleteMapping("/artistas/{id}/redes/{id_red}")
    public ResponseEntity<SuccessfulResponseDTO> eliminarRedSocial(
            @PathVariable Long id,
            @PathVariable("id_red") Long idRed,
            Authentication authentication) {

        Long authenticatedUserId = Long.parseLong(authentication.getName());

        redSocialService.eliminarRedSocial(id, idRed, authenticatedUserId);

        SuccessfulResponseDTO response = SuccessfulResponseDTO.builder()
                .successful("Eliminación de red social exitosa")
                .message("La red social ha sido eliminada correctamente")
                .statusCode(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.ok(response);
    }
}