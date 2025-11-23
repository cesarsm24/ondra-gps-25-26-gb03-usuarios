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
 * Controlador REST para gestión de métodos de pago de usuarios.
 *
 * <p>Permite a los usuarios administrar sus métodos de pago para realizar compras.
 * Los tipos permitidos son: TARJETA, PAYPAL, BIZUM y TRANSFERENCIA.
 * Todos los endpoints requieren autenticación JWT.</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/usuarios/{id}/metodos-pago")
public class MetodoPagoUsuarioController {

    private final MetodoPagoUsuarioService metodoPagoService;

    /**
     * Lista todos los métodos de pago de un usuario.
     *
     * <p>Solo el propietario puede consultar sus métodos de pago.</p>
     *
     * @param id ID del usuario
     * @param authentication Autenticación del usuario
     * @return Lista de métodos de pago
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
     * Crea un nuevo método de pago para el usuario.
     *
     * <p>Tipos permitidos: TARJETA, PAYPAL, BIZUM, TRANSFERENCIA.</p>
     *
     * @param id ID del usuario
     * @param crearDTO Datos del método de pago
     * @param authentication Autenticación del usuario
     * @return Método de pago creado
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
     * Actualiza un método de pago existente.
     *
     * @param id ID del usuario
     * @param idMetodo ID del método de pago
     * @param editarDTO Datos actualizados
     * @param authentication Autenticación del usuario
     * @return Método de pago actualizado
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
     * @param id ID del usuario
     * @param idMetodo ID del método de pago
     * @param authentication Autenticación del usuario
     * @return Confirmación de eliminación
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