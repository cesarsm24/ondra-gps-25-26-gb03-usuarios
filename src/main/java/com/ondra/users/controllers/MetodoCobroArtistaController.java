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
 * Controlador REST para gestión de métodos de cobro de artistas.
 *
 * <p>Permite a los artistas administrar sus métodos de cobro para recibir pagos
 * por sus contenidos. Los tipos permitidos son: PAYPAL, BIZUM y TRANSFERENCIA.
 * Todos los endpoints requieren autenticación JWT.</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/artistas/{id}/metodos-cobro")
public class MetodoCobroArtistaController {

    private final MetodoCobroArtistaService metodoCobroService;

    /**
     * Lista todos los métodos de cobro de un artista.
     *
     * <p>Solo el propietario puede consultar sus métodos de cobro.</p>
     *
     * @param id ID del artista
     * @param authentication Autenticación del usuario
     * @return Lista de métodos de cobro
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
     * Crea un nuevo método de cobro para el artista.
     *
     * <p>Tipos permitidos: PAYPAL, BIZUM, TRANSFERENCIA.</p>
     *
     * @param id ID del artista
     * @param crearDTO Datos del método de cobro
     * @param authentication Autenticación del usuario
     * @return Método de cobro creado
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
     * Actualiza un método de cobro existente.
     *
     * @param id ID del artista
     * @param idMetodo ID del método de cobro
     * @param editarDTO Datos actualizados
     * @param authentication Autenticación del usuario
     * @return Método de cobro actualizado
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
     * @param id ID del artista
     * @param idMetodo ID del método de cobro
     * @param authentication Autenticación del usuario
     * @return Confirmación de eliminación
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