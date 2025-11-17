package com.ondra.users.controllers;

import com.ondra.users.dto.MetodoCobroArtistaCrearDTO;
import com.ondra.users.dto.MetodoCobroArtistaDTO;
import com.ondra.users.dto.MetodoCobroArtistaEditarDTO;
import com.ondra.users.services.MetodoCobroArtistaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de métodos de cobro de artistas.
 *
 * <p>Proporciona endpoints para operaciones CRUD sobre métodos de cobro.
 * Los artistas únicamente pueden utilizar PAYPAL, BIZUM o TRANSFERENCIA como métodos de cobro.
 * El tipo TARJETA está restringido para este perfil de usuario.</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/artistas/{id}/metodos-cobro")
public class MetodoCobroArtistaController {

    private final MetodoCobroArtistaService metodoCobroService;

    /**
     * Obtiene todos los métodos de cobro asociados a un artista.
     *
     * <p>Requiere que el usuario autenticado sea el propietario del recurso.</p>
     *
     * @param id identificador del artista
     * @param authentication contexto de autenticación del usuario
     * @return lista de métodos de cobro del artista
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MetodoCobroArtistaDTO>> listarMetodosCobro(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long authenticatedUserId = Long.parseLong(authentication.getName());
        List<MetodoCobroArtistaDTO> metodos = metodoCobroService.listarMetodosCobro(id, authenticatedUserId);
        return ResponseEntity.ok(metodos);
    }

    /**
     * Crea un método de cobro para el artista especificado.
     *
     * <p>Valida que el tipo de método sea compatible con el perfil de artista.
     * Los tipos permitidos son: PAYPAL, BIZUM, TRANSFERENCIA.</p>
     *
     * @param id identificador del artista
     * @param crearDTO datos del método de cobro a crear
     * @param authentication contexto de autenticación del usuario
     * @return método de cobro creado con código HTTP 201
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MetodoCobroArtistaDTO> crearMetodoCobro(
            @PathVariable Long id,
            @Valid @RequestBody MetodoCobroArtistaCrearDTO crearDTO,
            Authentication authentication
    ) {
        Long authenticatedUserId = Long.parseLong(authentication.getName());
        MetodoCobroArtistaDTO metodoCreado = metodoCobroService.crearMetodoCobro(id, crearDTO, authenticatedUserId);
        return new ResponseEntity<>(metodoCreado, HttpStatus.CREATED);
    }

    /**
     * Actualiza los datos de un método de cobro existente.
     *
     * <p>Permite modificar únicamente los campos específicos del tipo de método seleccionado.</p>
     *
     * @param id identificador del artista
     * @param idMetodo identificador del método de cobro
     * @param editarDTO datos actualizados del método
     * @param authentication contexto de autenticación del usuario
     * @return método de cobro actualizado
     */
    @PutMapping(value = "/{id_metodo}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MetodoCobroArtistaDTO> editarMetodoCobro(
            @PathVariable Long id,
            @PathVariable("id_metodo") Long idMetodo,
            @Valid @RequestBody MetodoCobroArtistaEditarDTO editarDTO,
            Authentication authentication
    ) {
        Long authenticatedUserId = Long.parseLong(authentication.getName());
        MetodoCobroArtistaDTO metodoActualizado = metodoCobroService.editarMetodoCobro(id, idMetodo, editarDTO, authenticatedUserId);
        return ResponseEntity.ok(metodoActualizado);
    }

    /**
     * Elimina un método de cobro del artista.
     *
     * @param id identificador del artista
     * @param idMetodo identificador del método de cobro a eliminar
     * @param authentication contexto de autenticación del usuario
     * @return mensaje de confirmación de eliminación
     */
    @DeleteMapping("/{id_metodo}")
    public ResponseEntity<String> eliminarMetodoCobro(
            @PathVariable Long id,
            @PathVariable("id_metodo") Long idMetodo,
            Authentication authentication
    ) {
        Long authenticatedUserId = Long.parseLong(authentication.getName());
        metodoCobroService.eliminarMetodoCobro(id, idMetodo, authenticatedUserId);
        return ResponseEntity.ok("Método de cobro eliminado correctamente");
    }
}