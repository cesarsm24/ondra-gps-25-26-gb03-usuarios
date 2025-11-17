package com.ondra.users.controllers;

import com.ondra.users.dto.MetodoPagoUsuarioCrearDTO;
import com.ondra.users.dto.MetodoPagoUsuarioDTO;
import com.ondra.users.dto.MetodoPagoUsuarioEditarDTO;
import com.ondra.users.services.MetodoPagoUsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de métodos de pago de usuarios.
 *
 * <p>Proporciona endpoints para operaciones CRUD sobre métodos de pago.
 * Los usuarios pueden utilizar TARJETA, PAYPAL, BIZUM o TRANSFERENCIA como métodos de pago.</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/usuarios/{id}/metodos-pago")
public class MetodoPagoUsuarioController {

    private final MetodoPagoUsuarioService metodoPagoService;

    /**
     * Obtiene todos los métodos de pago asociados a un usuario.
     *
     * <p>Requiere que el usuario autenticado sea el propietario del recurso.</p>
     *
     * @param id identificador del usuario
     * @param authentication contexto de autenticación del usuario
     * @return lista de métodos de pago del usuario
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MetodoPagoUsuarioDTO>> listarMetodosPago(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long authenticatedUserId = Long.parseLong(authentication.getName());
        List<MetodoPagoUsuarioDTO> metodos = metodoPagoService.listarMetodosPago(id, authenticatedUserId);
        return ResponseEntity.ok(metodos);
    }

    /**
     * Crea un método de pago para el usuario especificado.
     *
     * <p>Los tipos permitidos son: TARJETA, PAYPAL, BIZUM, TRANSFERENCIA.</p>
     *
     * @param id identificador del usuario
     * @param crearDTO datos del método de pago a crear
     * @param authentication contexto de autenticación del usuario
     * @return método de pago creado con código HTTP 201
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MetodoPagoUsuarioDTO> crearMetodoPago(
            @PathVariable Long id,
            @Valid @RequestBody MetodoPagoUsuarioCrearDTO crearDTO,
            Authentication authentication
    ) {
        Long authenticatedUserId = Long.parseLong(authentication.getName());
        MetodoPagoUsuarioDTO metodoCreado = metodoPagoService.crearMetodoPago(id, crearDTO, authenticatedUserId);
        return new ResponseEntity<>(metodoCreado, HttpStatus.CREATED);
    }

    /**
     * Actualiza los datos de un método de pago existente.
     *
     * <p>Permite modificar únicamente los campos específicos del tipo de método seleccionado.</p>
     *
     * @param id identificador del usuario
     * @param idMetodo identificador del método de pago
     * @param editarDTO datos actualizados del método
     * @param authentication contexto de autenticación del usuario
     * @return método de pago actualizado
     */
    @PutMapping(value = "/{id_metodo}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MetodoPagoUsuarioDTO> editarMetodoPago(
            @PathVariable Long id,
            @PathVariable("id_metodo") Long idMetodo,
            @Valid @RequestBody MetodoPagoUsuarioEditarDTO editarDTO,
            Authentication authentication
    ) {
        Long authenticatedUserId = Long.parseLong(authentication.getName());
        MetodoPagoUsuarioDTO metodoActualizado = metodoPagoService.editarMetodoPago(id, idMetodo, editarDTO, authenticatedUserId);
        return ResponseEntity.ok(metodoActualizado);
    }

    /**
     * Elimina un método de pago del usuario.
     *
     * @param id identificador del usuario
     * @param idMetodo identificador del método de pago a eliminar
     * @param authentication contexto de autenticación del usuario
     * @return mensaje de confirmación de eliminación
     */
    @DeleteMapping("/{id_metodo}")
    public ResponseEntity<String> eliminarMetodoPago(
            @PathVariable Long id,
            @PathVariable("id_metodo") Long idMetodo,
            Authentication authentication
    ) {
        Long authenticatedUserId = Long.parseLong(authentication.getName());
        metodoPagoService.eliminarMetodoPago(id, idMetodo, authenticatedUserId);
        return ResponseEntity.ok("Método de pago eliminado correctamente");
    }
}