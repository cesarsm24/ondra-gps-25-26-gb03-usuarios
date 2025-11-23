package com.ondra.users.controllers;

import com.ondra.users.dto.EstadisticasSeguimientoDTO;
import com.ondra.users.dto.SeguimientoDTO;
import com.ondra.users.dto.SeguirUsuarioDTO;
import com.ondra.users.dto.UsuarioBasicoDTO;
import com.ondra.users.services.SeguimientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para la gestión de seguimientos entre usuarios.
 *
 * <p>Reglas de negocio:</p>
 * <ul>
 *   <li>Solo usuarios normales pueden seguir a otros</li>
 *   <li>Los artistas no pueden seguir, solo ser seguidos</li>
 *   <li>No se permite seguir a uno mismo</li>
 *   <li>No se permiten seguimientos duplicados</li>
 * </ul>
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/seguimientos")
public class SeguimientoController {

    private final SeguimientoService seguimientoService;

    /**
     * Crea un nuevo seguimiento.
     *
     * <p>El usuario seguidor se obtiene del token de autenticación.</p>
     *
     * @param dto Datos del usuario a seguir
     * @param authentication Contexto de autenticación
     * @return Seguimiento creado
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SeguimientoDTO> seguirUsuario(
            @Valid @RequestBody SeguirUsuarioDTO dto,
            Authentication authentication) {

        Long authenticatedUserId = Long.parseLong(authentication.getName());

        SeguimientoDTO seguimiento = seguimientoService.seguirUsuario(
                authenticatedUserId,
                dto.getIdUsuarioASeguir(),
                authenticatedUserId
        );

        return new ResponseEntity<>(seguimiento, HttpStatus.CREATED);
    }

    /**
     * Elimina un seguimiento existente.
     *
     * @param idUsuario Identificador del usuario a dejar de seguir
     * @param authentication Contexto de autenticación
     * @return Respuesta vacía
     */
    @DeleteMapping("/{idUsuario}")
    public ResponseEntity<Void> dejarDeSeguir(
            @PathVariable Long idUsuario,
            Authentication authentication) {

        Long authenticatedUserId = Long.parseLong(authentication.getName());

        seguimientoService.dejarDeSeguir(
                authenticatedUserId,
                idUsuario,
                authenticatedUserId
        );

        return ResponseEntity.ok().build();
    }

    /**
     * Lista los usuarios que sigue un usuario específico.
     *
     * @param idUsuario Identificador del usuario
     * @return Lista de usuarios seguidos
     */
    @GetMapping("/{idUsuario}/seguidos")
    public ResponseEntity<List<UsuarioBasicoDTO>> obtenerSeguidos(
            @PathVariable Long idUsuario) {

        List<UsuarioBasicoDTO> seguidos = seguimientoService.obtenerSeguidos(idUsuario);
        return ResponseEntity.ok(seguidos);
    }

    /**
     * Lista los seguidores de un usuario específico.
     *
     * @param idUsuario Identificador del usuario
     * @return Lista de seguidores
     */
    @GetMapping("/{idUsuario}/seguidores")
    public ResponseEntity<List<UsuarioBasicoDTO>> obtenerSeguidores(
            @PathVariable Long idUsuario) {

        List<UsuarioBasicoDTO> seguidores = seguimientoService.obtenerSeguidores(idUsuario);
        return ResponseEntity.ok(seguidores);
    }

    /**
     * Obtiene las estadísticas de seguimiento de un usuario.
     *
     * <p>Incluye cantidad de seguidos y seguidores.</p>
     *
     * @param idUsuario Identificador del usuario
     * @return Estadísticas de seguimiento
     */
    @GetMapping("/{idUsuario}/estadisticas")
    public ResponseEntity<EstadisticasSeguimientoDTO> obtenerEstadisticas(
            @PathVariable Long idUsuario) {

        EstadisticasSeguimientoDTO estadisticas =
                seguimientoService.obtenerEstadisticas(idUsuario);
        return ResponseEntity.ok(estadisticas);
    }

    /**
     * Verifica si el usuario autenticado sigue a otro usuario.
     *
     * @param idUsuario Identificador del usuario a verificar
     * @param authentication Contexto de autenticación
     * @return true si existe el seguimiento, false en caso contrario
     */
    @GetMapping("/{idUsuario}/verificar")
    public ResponseEntity<Boolean> verificarSeguimiento(
            @PathVariable Long idUsuario,
            Authentication authentication) {

        Long authenticatedUserId = Long.parseLong(authentication.getName());

        boolean sigue = seguimientoService.verificarSeguimiento(
                authenticatedUserId,
                idUsuario
        );

        return ResponseEntity.ok(sigue);
    }
}