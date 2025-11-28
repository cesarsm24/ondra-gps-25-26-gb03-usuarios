package com.ondra.users.controllers;

import com.ondra.users.dto.*;
import com.ondra.users.exceptions.ForbiddenAccessException;
import com.ondra.users.models.enums.TipoUsuario;
import com.ondra.users.services.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión de usuarios.
 * Incluye operaciones de registro, autenticación, perfil y administración de credenciales.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * Registra un nuevo usuario y envía un correo de verificación.
     *
     * @param registroDTO datos del usuario a registrar
     * @return usuario creado
     */
    @PostMapping(value = "/usuarios", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UsuarioDTO> registrarUsuario(@Valid @RequestBody RegistroUsuarioDTO registroDTO) {
        UsuarioDTO usuario = usuarioService.registrarUsuario(registroDTO);
        return new ResponseEntity<>(usuario, HttpStatus.CREATED);
    }

    /**
     * Verifica el email mediante un token enviado al usuario.
     *
     * @param token token de verificación
     * @return mensaje de confirmación
     */
    @GetMapping("/usuarios/verificar-email")
    public ResponseEntity<String> verificarEmail(@RequestParam String token) {
        usuarioService.verificarEmail(token);
        return ResponseEntity.ok("Email verificado correctamente. Ya puedes iniciar sesión");
    }

    /**
     * Reenvía el correo de verificación.
     *
     * @param reenviarDTO datos del destinatario
     * @return mensaje de confirmación
     */
    @PostMapping("/usuarios/reenviar-verificacion")
    public ResponseEntity<String> reenviarVerificacion(@Valid @RequestBody ReenviarVerificacionDTO reenviarDTO) {
        usuarioService.reenviarEmailVerificacion(reenviarDTO);
        return ResponseEntity.ok("Correo de verificación reenviado");
    }

    /**
     * Realiza autenticación mediante email y contraseña.
     *
     * @param loginDTO credenciales del usuario
     * @return token de acceso y datos del usuario
     */
    @PostMapping(value = "/usuarios/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponseDTO> loginUsuario(@Valid @RequestBody LoginUsuarioDTO loginDTO) {
        AuthResponseDTO authResponse = usuarioService.loginUsuario(loginDTO);
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Autentica o registra un usuario mediante Google.
     *
     * @param loginGoogleDTO token de autenticación de Google
     * @return token de acceso y datos del usuario
     */
    @PostMapping(value = "/usuarios/login/google", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponseDTO> loginGoogle(@Valid @RequestBody LoginGoogleDTO loginGoogleDTO) {
        AuthResponseDTO authResponse = usuarioService.loginGoogle(loginGoogleDTO);
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Genera un nuevo access token mediante refresh token válido.
     *
     * @param request refresh token recibido
     * @return nuevos tokens
     */
    @PostMapping(value = "/usuarios/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RefreshTokenResponseDTO> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDTO request) {

        RefreshTokenResponseDTO response =
                usuarioService.renovarAccessToken(request.getRefreshToken());

        return ResponseEntity.ok(response);
    }

    /**
     * Revoca un refresh token y cierra la sesión asociada.
     *
     * @param request refresh token a revocar
     * @return respuesta vacía
     */
    @PostMapping("/usuarios/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequestDTO request) {
        usuarioService.logout(request.getRefreshToken());
        return ResponseEntity.ok().build();
    }

    /**
     * Cierra todas las sesiones activas del usuario.
     *
     * @param authentication autenticación actual
     * @return respuesta vacía
     */
    @PostMapping("/usuarios/logout-all")
    public ResponseEntity<Void> logoutAll(Authentication authentication) {
        Long authenticatedUserId = Long.parseLong(authentication.getName());
        usuarioService.logoutGlobal(authenticatedUserId);
        return ResponseEntity.ok().build();
    }

    /**
     * Solicita un enlace para recuperar la contraseña.
     *
     * @param dto email del usuario
     * @return mensaje de confirmación
     */
    @PostMapping("/usuarios/recuperar-password")
    public ResponseEntity<String> recuperarPassword(
            @Valid @RequestBody RecuperarPasswordDTO dto) {

        usuarioService.solicitarRecuperacionPassword(dto);
        return ResponseEntity.ok("Si el email existe, recibirás un enlace de recuperación");
    }

    /**
     * Restablece la contraseña mediante un token de recuperación.
     *
     * @param dto token y nueva contraseña
     * @return mensaje de confirmación
     */
    @PostMapping("/usuarios/restablecer-password")
    public ResponseEntity<String> restablecerPassword(
            @Valid @RequestBody RestablecerPasswordDTO dto) {

        usuarioService.restablecerPassword(dto);
        return ResponseEntity.ok("Contraseña restablecida correctamente");
    }

    /**
     * Obtiene estadísticas globales del sistema.
     *
     * @return datos agregados de usuarios
     */
    @GetMapping(value = "/usuarios/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EstadisticasGlobalesDTO> obtenerStats() {
        EstadisticasGlobalesDTO stats = usuarioService.obtenerStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Obtiene el perfil de un usuario autenticado.
     *
     * @param id identificador del usuario
     * @param authentication autenticación actual
     * @return datos del usuario
     */
    @GetMapping(value = "/usuarios/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UsuarioDTO> obtenerUsuario(
            @PathVariable Long id,
            Authentication authentication) {

        Long authenticatedUserId = Long.parseLong(authentication.getName());
        UsuarioDTO usuario = usuarioService.obtenerUsuario(id, authenticatedUserId);
        return ResponseEntity.ok(usuario);
    }

    /**
     * Actualiza datos del usuario autenticado.
     *
     * @param id identificador del usuario
     * @param editarDTO datos a modificar
     * @param authentication autenticación actual
     * @return usuario actualizado
     */
    @PutMapping(value = "/usuarios/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UsuarioDTO> editarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody EditarUsuarioDTO editarDTO,
            Authentication authentication) {

        Long authenticatedUserId = Long.parseLong(authentication.getName());
        UsuarioDTO usuario = usuarioService.editarUsuario(id, editarDTO, authenticatedUserId);
        return ResponseEntity.ok(usuario);
    }

    /**
     * Cambia la contraseña de un usuario autenticado.
     *
     * @param id identificador del usuario
     * @param dto credenciales de cambio de contraseña
     * @param authentication autenticación actual
     * @return mensaje de confirmación
     */
    @PutMapping("/usuarios/{id}/cambiar-password")
    public ResponseEntity<String> cambiarPassword(
            @PathVariable Long id,
            @Valid @RequestBody CambiarPasswordDTO dto,
            Authentication authentication) {

        Long authenticatedUserId = Long.parseLong(authentication.getName());
        usuarioService.cambiarPassword(id, dto, authenticatedUserId);
        return ResponseEntity.ok("Contraseña cambiada correctamente");
    }

    /**
     * Elimina la cuenta del usuario autenticado.
     *
     * @param id identificador del usuario
     * @param authentication autenticación actual
     * @return respuesta vacía
     */
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> eliminarUsuario(
            @PathVariable Long id,
            Authentication authentication) {

        Long authenticatedUserId = Long.parseLong(authentication.getName());
        usuarioService.eliminarUsuario(id, authenticatedUserId);
        return ResponseEntity.ok().build();
    }

    /**
     * Marca el onboarding como completado.
     *
     * @param id identificador del usuario
     * @param authentication autenticación actual
     * @return respuesta vacía
     */
    @PatchMapping("/usuarios/{id}/onboarding-completado")
    public ResponseEntity<Void> marcarOnboardingCompletado(
            @PathVariable Long id,
            Authentication authentication) {

        Long authenticatedUserId = Long.parseLong(authentication.getName());

        if (!authenticatedUserId.equals(id)) {
            throw new ForbiddenAccessException("No puedes modificar el onboarding de otro usuario");
        }

        usuarioService.marcarOnboardingCompletado(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Obtiene los datos básicos de un usuario o artista.
     *
     * @param id identificador del artista o usuario según el tipo
     * @param tipo tipo de usuario (ARTISTA o NORMAL)
     * @return datos básicos del usuario
     */
    @GetMapping(value = "/usuarios/{id}/datos-usuario", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DatosUsuarioDTO> obtenerDatosUsuario(
            @PathVariable Long id,
            @RequestParam TipoUsuario tipo) {

        return ResponseEntity.ok(usuarioService.obtenerDatosUsuario(id, tipo));
    }
}
