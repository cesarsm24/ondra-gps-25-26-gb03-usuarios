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
 * Controlador para la gestión de usuarios del sistema.
 *
 * <p>Proporciona endpoints para autenticación, registro, perfil y recuperación de contraseña.</p>
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * <p>Envía automáticamente un correo de verificación.
     * La cuenta estará activa pero no podrá hacer login hasta verificar el email.</p>
     *
     * @param registroDTO Datos del usuario a registrar
     * @return Usuario creado
     */
    @PostMapping(value = "/usuarios", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UsuarioDTO> registrarUsuario(@Valid @RequestBody RegistroUsuarioDTO registroDTO) {
        UsuarioDTO usuario = usuarioService.registrarUsuario(registroDTO);
        return new ResponseEntity<>(usuario, HttpStatus.CREATED);
    }

    /**
     * Verifica el email de un usuario mediante token.
     *
     * @param token Token de verificación recibido por correo
     * @return Mensaje de confirmación
     */
    @GetMapping("/usuarios/verificar-email")
    public ResponseEntity<String> verificarEmail(@RequestParam String token) {
        usuarioService.verificarEmail(token);
        return ResponseEntity.ok("Email verificado correctamente. Ya puedes iniciar sesión");
    }

    /**
     * Reenvía el correo de verificación de email.
     *
     * @param reenviarDTO Email del usuario
     * @return Mensaje de confirmación
     */
    @PostMapping("/usuarios/reenviar-verificacion")
    public ResponseEntity<String> reenviarVerificacion(@Valid @RequestBody ReenviarVerificacionDTO reenviarDTO) {
        usuarioService.reenviarEmailVerificacion(reenviarDTO);
        return ResponseEntity.ok("Correo de verificación reenviado. Revisa tu bandeja de entrada");
    }

    /**
     * Autentica un usuario mediante email y contraseña.
     *
     * <p>Solo permite login si el email ha sido verificado.</p>
     *
     * @param loginDTO Credenciales de acceso
     * @return Token JWT y datos del usuario
     */
    @PostMapping(value = "/usuarios/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponseDTO> loginUsuario(@Valid @RequestBody LoginUsuarioDTO loginDTO) {
        AuthResponseDTO authResponse = usuarioService.loginUsuario(loginDTO);
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Autentica o registra un usuario mediante token de Google.
     *
     * <p>Los usuarios de Google no requieren verificación de email.</p>
     *
     * @param loginGoogleDTO Token de autenticación de Google
     * @return Token JWT y datos del usuario
     */
    @PostMapping(value = "/usuarios/login/google", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponseDTO> loginGoogle(@Valid @RequestBody LoginGoogleDTO loginGoogleDTO) {
        AuthResponseDTO authResponse = usuarioService.loginGoogle(loginGoogleDTO);
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Renueva un access token mediante refresh token.
     *
     * @param request Refresh token válido
     * @return Nuevo access token y refresh token
     */
    @PostMapping(value = "/usuarios/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RefreshTokenResponseDTO> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDTO request) {
        RefreshTokenResponseDTO response = usuarioService.renovarAccessToken(
                request.getRefreshToken()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Cierra la sesión de un usuario revocando su refresh token.
     *
     * @param request Refresh token a revocar
     * @return Respuesta vacía
     */
    @PostMapping("/usuarios/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequestDTO request) {
        usuarioService.logout(request.getRefreshToken());
        return ResponseEntity.ok().build();
    }

    /**
     * Cierra todas las sesiones activas de un usuario.
     *
     * @param authentication Contexto de autenticación
     * @return Respuesta vacía
     */
    @PostMapping("/usuarios/logout-all")
    public ResponseEntity<Void> logoutAll(Authentication authentication) {
        Long authenticatedUserId = Long.parseLong(authentication.getName());
        usuarioService.logoutGlobal(authenticatedUserId);
        return ResponseEntity.ok().build();
    }

    /**
     * Solicita un enlace de recuperación de contraseña.
     *
     * @param dto Email del usuario
     * @return Mensaje de confirmación
     */
    @PostMapping("/usuarios/recuperar-password")
    public ResponseEntity<String> recuperarPassword(
            @Valid @RequestBody RecuperarPasswordDTO dto) {
        usuarioService.solicitarRecuperacionPassword(dto);
        return ResponseEntity.ok(
                "Si el email existe, recibirás un enlace de recuperación"
        );
    }

    /**
     * Restablece la contraseña mediante token de recuperación.
     *
     * @param dto Token y nueva contraseña
     * @return Mensaje de confirmación
     */
    @PostMapping("/usuarios/restablecer-password")
    public ResponseEntity<String> restablecerPassword(
            @Valid @RequestBody RestablecerPasswordDTO dto) {
        usuarioService.restablecerPassword(dto);
        return ResponseEntity.ok("Contraseña restablecida correctamente");
    }

    /**
     * Obtiene estadísticas de usuarios activos del sistema.
     *
     * @return Total de usuarios normales y artistas
     */
    @GetMapping(value = "/usuarios/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EstadisticasGlobalesDTO> obtenerStats() {
        EstadisticasGlobalesDTO stats = usuarioService.obtenerStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Obtiene el perfil de un usuario.
     *
     * <p>Solo puedes obtener tu propio perfil.</p>
     *
     * @param id Identificador del usuario
     * @param authentication Contexto de autenticación
     * @return Datos del usuario
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
     * Actualiza los datos del perfil de un usuario.
     *
     * <p>Solo el propietario puede editar su perfil.</p>
     *
     * @param id Identificador del usuario
     * @param editarDTO Datos a actualizar
     * @param authentication Contexto de autenticación
     * @return Usuario actualizado
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
     * @param id Identificador del usuario
     * @param dto Contraseña actual y nueva contraseña
     * @param authentication Contexto de autenticación
     * @return Mensaje de confirmación
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
     * Elimina la cuenta de un usuario.
     *
     * <p>Solo el propietario puede eliminar su cuenta.</p>
     *
     * @param id Identificador del usuario
     * @param authentication Contexto de autenticación
     * @return Respuesta vacía
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
     * <p>Se ejecuta al completar la configuración inicial o al omitir el wizard.</p>
     *
     * @param id Identificador del usuario
     * @param authentication Contexto de autenticación
     * @return Respuesta vacía
     */
    @PatchMapping("/usuarios/{id}/onboarding-completado")
    public ResponseEntity<Void> marcarOnboardingCompletado(
            @PathVariable Long id,
            Authentication authentication) {
        Long authenticatedUserId = Long.parseLong(authentication.getName());

        if (!authenticatedUserId.equals(id)) {
            throw new ForbiddenAccessException(
                    "No puedes modificar el onboarding de otro usuario"
            );
        }

        usuarioService.marcarOnboardingCompletado(id);

        return ResponseEntity.ok().build();
    }

    /**
     * Obtiene el nombre completo de un usuario o artista.
     *
     * @param id   Identificador
     * @param tipo TipoUsuario (ARTISTA o USUARIO)
     */
    @GetMapping(value = "/usuarios/{id}/nombre-completo", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NombreUsuarioDTO> obtenerNombreCompleto(
            @PathVariable Long id,
            @RequestParam TipoUsuario tipo) {

        return ResponseEntity.ok(usuarioService.obtenerNombreCompleto(id, tipo));
    }
}