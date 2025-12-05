package com.ondra.users.controllers;

import com.ondra.users.dto.RedSocialCrearDTO;
import com.ondra.users.dto.RedSocialDTO;
import com.ondra.users.dto.RedSocialEditarDTO;
import com.ondra.users.dto.SuccessfulResponseDTO;
import com.ondra.users.services.RedSocialService;
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
 * Controlador para la gestión de redes sociales de artistas.
 *
 * <p>Proporciona endpoints para operaciones CRUD sobre redes sociales.</p>
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class RedSocialController {

    private final RedSocialService redSocialService;

    /**
     * Lista todas las redes sociales de un artista.
     *
     * @param id Identificador del artista
     * @return Lista de redes sociales
     */
    @GetMapping(value = "/artistas/{id}/redes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RedSocialDTO>> listarRedesSociales(@PathVariable Long id) {
        List<RedSocialDTO> redesSociales = redSocialService.listarRedesSociales(id);
        return ResponseEntity.ok(redesSociales);
    }

    /**
     * Crea una nueva red social para un artista.
     *
     * <p>Requiere autenticación. El usuario autenticado debe ser el propietario del perfil.</p>
     *
     * @param id Identificador del artista
     * @param crearDTO Datos de la red social a crear
     * @param authentication Contexto de autenticación
     * @return Red social creada
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
     * Actualiza una red social existente.
     *
     * <p>Requiere autenticación. El usuario autenticado debe ser el propietario del perfil.</p>
     *
     * @param id Identificador del artista
     * @param idRed Identificador de la red social
     * @param editarDTO Datos a actualizar
     * @param authentication Contexto de autenticación
     * @return Red social actualizada
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
     * Elimina una red social de un artista.
     *
     * <p>Requiere autenticación. El usuario autenticado debe ser el propietario del perfil.</p>
     *
     * @param id Identificador del artista
     * @param idRed Identificador de la red social
     * @param authentication Contexto de autenticación
     * @return Respuesta de confirmación
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