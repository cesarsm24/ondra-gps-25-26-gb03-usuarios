package com.ondra.users.services;

import com.google.firebase.auth.FirebaseAuth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.util.Collections;

import com.ondra.users.clients.ContenidosClient;
import com.ondra.users.clients.RecomendacionesClient;
import com.ondra.users.dto.*;
import com.ondra.users.exceptions.*;
import com.ondra.users.models.dao.*;
import com.ondra.users.models.enums.TipoUsuario;
import com.ondra.users.repositories.*;
import com.ondra.users.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final SeguimientoRepository seguimientoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final FirebaseAuth firebaseAuth;
    private final CloudinaryService cloudinaryService;
    private final EmailService emailService;

    // Clientes para comunicación con otros microservicios
    private final ContenidosClient contenidosClient;
    private final RecomendacionesClient recomendacionesClient;

    /**
     * Registra un nuevo usuario en el sistema con email y contraseña.
     * Envía un correo de verificación automáticamente.
     * La cuenta estará activa pero NO podrá hacer login hasta verificar.
     *
     * @param registroDTO Datos del nuevo usuario
     * @return {@link UsuarioDTO} con los datos del usuario creado
     * @throws EmailAlreadyExistsException Si el email ya está registrado
     */
    @Transactional
    public UsuarioDTO registrarUsuario(RegistroUsuarioDTO registroDTO) {
        if (usuarioRepository.findByEmailUsuario(registroDTO.getEmailUsuario()).isPresent()) {
            log.warn("Intento de registro con email duplicado: {}", registroDTO.getEmailUsuario());
            throw new EmailAlreadyExistsException("El email " + registroDTO.getEmailUsuario() + " ya está registrado");
        }

        // Generar token de verificación único
        String tokenVerificacion = UUID.randomUUID().toString();
        LocalDateTime fechaExpiracion = LocalDateTime.now().plusHours(24); // Token válido 24 horas

        Usuario usuario = Usuario.builder()
                .emailUsuario(registroDTO.getEmailUsuario())
                .passwordUsuario(passwordEncoder.encode(registroDTO.getPasswordUsuario()))
                .nombreUsuario(registroDTO.getNombreUsuario())
                .apellidosUsuario(registroDTO.getApellidosUsuario())
                .tipoUsuario(TipoUsuario.valueOf(registroDTO.getTipoUsuario().toString().toUpperCase()))
                .fechaRegistro(LocalDateTime.now())
                .activo(true)
                .emailVerificado(false) // Cuenta NO verificada inicialmente
                .tokenVerificacion(tokenVerificacion)
                .fechaExpiracionToken(fechaExpiracion)
                .permiteGoogle(false)
                .build();

        usuario = usuarioRepository.save(usuario);
        log.info("Usuario registrado exitosamente con ID: {} y email: {}", usuario.getIdUsuario(), usuario.getEmailUsuario());

        // Enviar correo de verificación
        try {
            emailService.enviarEmailVerificacion(
                    usuario.getEmailUsuario(),
                    usuario.getNombreUsuario(),
                    tokenVerificacion
            );
            log.info("Correo de verificación enviado a: {}", usuario.getEmailUsuario());
        } catch (Exception e) {
            log.error("Error al enviar correo de verificación: {}", e.getMessage());
            // No falla el registro si el email no se envía
        }

        return convertirAUsuarioDTO(usuario);
    }

    /**
     * Verifica el email de un usuario mediante el token recibido por correo.
     *
     * @param token Token de verificación
     * @throws InvalidVerificationTokenException Si el token es inválido o ha expirado
     */
    @Transactional
    public void verificarEmail(String token) {
        Usuario usuario = usuarioRepository.findByTokenVerificacion(token)
                .orElseThrow(() -> new InvalidVerificationTokenException("Token de verificación inválido"));

        if (usuario.getFechaExpiracionToken().isBefore(LocalDateTime.now())) {
            log.warn("Token de verificación expirado para usuario ID: {}", usuario.getIdUsuario());
            throw new InvalidVerificationTokenException("El token de verificación ha expirado");
        }

        if (usuario.isEmailVerificado()) {
            log.info("Intento de verificar email ya verificado para usuario ID: {}", usuario.getIdUsuario());
            return; // Ya está verificado, no hacer nada
        }

        usuario.setEmailVerificado(true);
        usuario.setTokenVerificacion(null); // Limpiar token usado
        usuario.setFechaExpiracionToken(null);
        usuarioRepository.save(usuario);

        log.info("Email verificado exitosamente para usuario ID: {}", usuario.getIdUsuario());
    }

    /**
     * Reenvía el correo de verificación a un usuario que no ha verificado su email.
     *
     * @param reenviarDTO Email del usuario
     * @throws UsuarioNotFoundException Si el usuario no existe
     * @throws EmailNotVerifiedException Si el email ya está verificado (código 403)
     */
    @Transactional
    public void reenviarEmailVerificacion(ReenviarVerificacionDTO reenviarDTO) {
        Usuario usuario = usuarioRepository.findByEmailUsuario(reenviarDTO.getEmailUsuario())
                .orElseThrow(() -> new UsuarioNotFoundException(
                        "No se encontró un usuario con el email " + reenviarDTO.getEmailUsuario()
                ));

        if (usuario.isEmailVerificado()) {
            throw new EmailNotVerifiedException("Este email ya está verificado");
        }

        // Generar nuevo token (el anterior podría haber expirado)
        String nuevoToken = UUID.randomUUID().toString();
        LocalDateTime nuevaFechaExpiracion = LocalDateTime.now().plusHours(24);

        usuario.setTokenVerificacion(nuevoToken);
        usuario.setFechaExpiracionToken(nuevaFechaExpiracion);
        usuarioRepository.save(usuario);

        // Enviar correo
        emailService.enviarEmailVerificacion(
                usuario.getEmailUsuario(),
                usuario.getNombreUsuario(),
                nuevoToken
        );

        log.info("Correo de verificación reenviado a: {}", usuario.getEmailUsuario());
    }

    /**
     * Autentica un usuario con email y contraseña, retornando JWT y refresh token.
     * SOLO permite login si el email ha sido verificado.
     *
     * @param loginDTO Credenciales del usuario
     * @return {@link AuthResponseDTO} con tokens y datos del usuario
     * @throws InvalidCredentialsException Si las credenciales son incorrectas
     * @throws AccountInactiveException Si la cuenta está inactiva
     * @throws EmailNotVerifiedException Si el email no ha sido verificado
     */
    @Transactional
    public AuthResponseDTO loginUsuario(LoginUsuarioDTO loginDTO) {
        Usuario usuario = usuarioRepository.findByEmailUsuario(loginDTO.getEmailUsuario())
                .orElseThrow(() -> {
                    log.warn("Intento de login con email no registrado: {}", loginDTO.getEmailUsuario());
                    return new InvalidCredentialsException("Email o contraseña incorrectos");
                });

        if (!usuario.isActivo()) {
            log.warn("Intento de login en cuenta inactiva. Usuario ID: {}", usuario.getIdUsuario());
            throw new AccountInactiveException("La cuenta está inactiva. Contacta con soporte");
        }

        // VALIDAR QUE EL EMAIL ESTÉ VERIFICADO
        if (!usuario.isEmailVerificado()) {
            log.warn("Intento de login sin verificar email. Usuario ID: {}", usuario.getIdUsuario());
            throw new EmailNotVerifiedException("Debes verificar tu email antes de iniciar sesión. Revisa tu bandeja de entrada");
        }

        if (usuario.getPasswordUsuario() == null || usuario.getPasswordUsuario().isEmpty()) {
            log.warn("Intento de login con contraseña en cuenta de Google. Usuario ID: {}", usuario.getIdUsuario());
            throw new InvalidCredentialsException("Esta cuenta solo permite login con Google");
        }

        if (!passwordEncoder.matches(loginDTO.getPasswordUsuario(), usuario.getPasswordUsuario())) {
            log.warn("Contraseña incorrecta para usuario: {}", loginDTO.getEmailUsuario());
            throw new InvalidCredentialsException("Email o contraseña incorrectos");
        }

        String token = jwtService.generarToken(
                usuario.getEmailUsuario(),
                usuario.getIdUsuario(),
                usuario.getTipoUsuario().name()
        );

        RefreshToken refreshToken = jwtService.generarRefreshToken(usuario);

        log.info("Login exitoso para usuario ID: {}", usuario.getIdUsuario());

        return AuthResponseDTO.builder()
                .token(token)
                .refreshToken(refreshToken.getToken())
                .usuario(convertirAUsuarioDTO(usuario))
                .build();
    }

    /**
     * Autentica o registra un usuario mediante token de Google OAuth.
     * Valida tokens de Google Sign-In directamente sin necesidad de Firebase.
     * Los usuarios de Google NO necesitan verificación de email (Google ya lo verifica).
     *
     * @param loginGoogleDTO Token de autenticación de Google
     * @return {@link AuthResponseDTO} con token JWT y datos del usuario
     */
    @Transactional
    public AuthResponseDTO loginGoogle(LoginGoogleDTO loginGoogleDTO) {
        String email;
        String nombre;
        String googleUid;
        String fotoPerfil = null;

        try {
            // Crear verificador de tokens de Google OAuth
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    new GsonFactory()
            )
                    .setAudience(Collections.singletonList(
                            "41556010027-4d8rs7q4ueggb72ql3v96maf9hn16cph.apps.googleusercontent.com"
                    ))
                    .build();

            // Verificar el token
            GoogleIdToken idToken = verifier.verify(loginGoogleDTO.getIdToken());

            if (idToken == null) {
                log.error("Token de Google inválido o expirado");
                throw new InvalidGoogleTokenException("El token de Google es inválido o ha expirado");
            }

            // Extraer información del payload
            GoogleIdToken.Payload payload = idToken.getPayload();
            googleUid = payload.getSubject(); // Google User ID único
            email = payload.getEmail();
            nombre = (String) payload.get("name");
            fotoPerfil = (String) payload.get("picture");

            // Validar que el email esté verificado por Google
            Boolean emailVerified = payload.getEmailVerified();
            if (emailVerified == null || !emailVerified) {
                log.warn("Intento de login con email no verificado por Google: {}", email);
                throw new InvalidGoogleTokenException("El email de Google no está verificado");
            }

            log.info("✅ Token de Google validado exitosamente para: {}", email);

        } catch (InvalidGoogleTokenException e) {
            throw e; // Re-lanzar excepciones propias
        } catch (Exception e) {
            log.error("❌ Error al validar token de Google: {}", e.getMessage(), e);
            throw new InvalidGoogleTokenException("El token de Google es inválido o ha expirado");
        }

        // Buscar usuario existente por Google UID o email
        Optional<Usuario> usuarioOpt = usuarioRepository.findByGoogleUid(googleUid);
        if (usuarioOpt.isEmpty()) {
            usuarioOpt = usuarioRepository.findByEmailUsuario(email);
        }

        Usuario usuario;

        if (usuarioOpt.isPresent()) {
            // Usuario existente
            usuario = usuarioOpt.get();

            // Verificar que la cuenta tenga habilitado login con Google
            if (!usuario.isPermiteGoogle()) {
                log.warn("Intento de login con Google en cuenta sin permiso. Usuario ID: {}", usuario.getIdUsuario());
                throw new GoogleLoginDisabledException("Esta cuenta no tiene habilitado el login con Google");
            }

            // Verificar que la cuenta esté activa
            if (!usuario.isActivo()) {
                log.warn("Intento de login con Google en cuenta inactiva. Usuario ID: {}", usuario.getIdUsuario());
                throw new AccountInactiveException("La cuenta está inactiva. Contacta con soporte");
            }

            // Vincular Google UID si no está vinculado
            if (usuario.getGoogleUid() == null || usuario.getGoogleUid().isEmpty()) {
                usuario.setGoogleUid(googleUid);
                usuario = usuarioRepository.save(usuario);
                log.info("🔗 Google UID vinculado al usuario ID: {}", usuario.getIdUsuario());
            }

            // Actualizar foto de perfil si es de Google y ha cambiado
            if (fotoPerfil != null && !fotoPerfil.equals(usuario.getFotoPerfil())) {
                usuario.setFotoPerfil(fotoPerfil);
                usuario = usuarioRepository.save(usuario);
                log.info("📸 Foto de perfil actualizada desde Google para usuario ID: {}", usuario.getIdUsuario());
            }

            log.info("✅ Login con Google exitoso para usuario ID: {}", usuario.getIdUsuario());

        } else {
            // Nuevo usuario - registrar automáticamente
            String[] nombreCompleto = separarNombreCompleto(nombre);

            usuario = Usuario.builder()
                    .emailUsuario(email)
                    .googleUid(googleUid)
                    .nombreUsuario(nombreCompleto[0])
                    .apellidosUsuario(nombreCompleto[1])
                    .tipoUsuario(TipoUsuario.NORMAL)
                    .fechaRegistro(LocalDateTime.now())
                    .activo(true)
                    .emailVerificado(true) // Google ya verifica el email
                    .permiteGoogle(true)
                    .fotoPerfil(fotoPerfil)
                    .build();

            usuario = usuarioRepository.save(usuario);
            log.info("🆕 Nuevo usuario registrado vía Google con ID: {} y email: {}",
                    usuario.getIdUsuario(), usuario.getEmailUsuario());
        }

        // Generar tokens JWT
        String token = jwtService.generarToken(
                usuario.getEmailUsuario(),
                usuario.getIdUsuario(),
                usuario.getTipoUsuario().name()
        );

        RefreshToken refreshToken = jwtService.generarRefreshToken(usuario);

        return AuthResponseDTO.builder()
                .token(token)
                .refreshToken(refreshToken.getToken())
                .usuario(convertirAUsuarioDTO(usuario))
                .build();
    }

    /**
     * Renueva un access token usando un refresh token válido.
     *
     * @param refreshTokenString String del refresh token
     * @return Nuevos access token y refresh token
     * @throws InvalidRefreshTokenException Si el refresh token es inválido
     */
    @Transactional
    public RefreshTokenResponseDTO renovarAccessToken(String refreshTokenString) {
        RefreshToken refreshToken = jwtService.validarRefreshToken(refreshTokenString);
        Usuario usuario = refreshToken.getUsuario();

        String nuevoAccessToken = jwtService.generarToken(
                usuario.getEmailUsuario(),
                usuario.getIdUsuario(),
                usuario.getTipoUsuario().name()
        );

        jwtService.revocarRefreshToken(refreshTokenString);
        RefreshToken nuevoRefreshToken = jwtService.generarRefreshToken(usuario);

        log.info("Access token renovado para usuario ID: {}", usuario.getIdUsuario());

        return RefreshTokenResponseDTO.builder()
                .accessToken(nuevoAccessToken)
                .refreshToken(nuevoRefreshToken.getToken())
                .build();
    }

    /**
     * Obtiene el perfil de un usuario.
     * Solo el propietario puede ver su perfil completo.
     *
     * @param id ID del usuario a obtener
     * @param authenticatedUserId ID del usuario autenticado
     * @return UsuarioDTO con los datos del perfil
     * @throws UsuarioNotFoundException Si el usuario no existe
     * @throws ForbiddenAccessException Si no es el propietario
     */
    @Transactional(readOnly = true)
    public UsuarioDTO obtenerUsuario(Long id, Long authenticatedUserId) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));

        if (!usuario.getIdUsuario().equals(authenticatedUserId)) {
            log.warn("Usuario ID: {} intentó acceder al perfil del usuario ID: {}", authenticatedUserId, id);
            throw new ForbiddenAccessException("No tienes permiso para modificar este perfil");
        }

        return convertirAUsuarioDTO(usuario);
    }

    /**
     * Cierra la sesión de un usuario revocando su refresh token.
     *
     * @param refreshToken Refresh token a revocar
     */
    @Transactional
    public void logout(String refreshToken) {
        jwtService.revocarRefreshToken(refreshToken);
        log.info("Sesión cerrada (refresh token revocado)");
    }

    /**
     * Cierra todas las sesiones activas de un usuario.
     *
     * @param idUsuario ID del usuario
     */
    @Transactional
    public void logoutGlobal(Long idUsuario) {
        jwtService.revocarTodosLosTokensDelUsuario(idUsuario);
        log.info("Todas las sesiones cerradas para usuario ID: {}", idUsuario);
    }

    /**
     * Edita el perfil de un usuario.
     * Solo el propietario puede editar su perfil.
     *
     * @param id ID del usuario a editar
     * @param editarDTO Datos a actualizar
     * @param authenticatedUserId ID del usuario autenticado
     * @return UsuarioDTO actualizado
     * @throws UsuarioNotFoundException Si el usuario no existe
     * @throws ForbiddenAccessException Si no es el propietario
     */
    @Transactional
    public UsuarioDTO editarUsuario(Long id, EditarUsuarioDTO editarDTO, Long authenticatedUserId) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));

        if (!usuario.getIdUsuario().equals(authenticatedUserId)) {
            log.warn("Usuario ID: {} intentó editar el perfil del usuario ID: {}", authenticatedUserId, id);
            throw new ForbiddenAccessException("No tienes permiso para modificar este perfil");
        }

        if (editarDTO.getNombreUsuario() != null && !editarDTO.getNombreUsuario().isEmpty()) {
            usuario.setNombreUsuario(editarDTO.getNombreUsuario());
        }

        if (editarDTO.getApellidosUsuario() != null && !editarDTO.getApellidosUsuario().isEmpty()) {
            usuario.setApellidosUsuario(editarDTO.getApellidosUsuario());
        }

        if (editarDTO.getFotoPerfil() != null && !editarDTO.getFotoPerfil().isEmpty()) {
            String fotoAntigua = usuario.getFotoPerfil();

            if (fotoAntigua != null && !fotoAntigua.isEmpty() &&
                    !fotoAntigua.contains("googleusercontent.com")) {
                cloudinaryService.eliminarImagen(fotoAntigua);
                log.info("Imagen anterior eliminada de Cloudinary: {}", fotoAntigua);
            }

            usuario.setFotoPerfil(editarDTO.getFotoPerfil());
        }

        usuario = usuarioRepository.save(usuario);
        log.info("Usuario ID: {} actualizado exitosamente", id);

        return convertirAUsuarioDTO(usuario);
    }

    /**
     * Elimina un usuario (soft delete).
     * Solo el propietario puede eliminar su cuenta.
     *
     * @param id ID del usuario a eliminar
     * @param authenticatedUserId ID del usuario autenticado
     * @throws UsuarioNotFoundException Si el usuario no existe
     * @throws ForbiddenAccessException Si no es el propietario
     */
    @Transactional
    public void eliminarUsuario(Long id, Long authenticatedUserId) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));

        if (!usuario.getIdUsuario().equals(authenticatedUserId)) {
            log.warn("Usuario ID: {} intentó eliminar la cuenta del usuario ID: {}", authenticatedUserId, id);
            throw new ForbiddenAccessException("No tienes permiso para modificar este perfil");
        }

        // Eliminar foto de perfil de Cloudinary
        if (usuario.getFotoPerfil() != null && !usuario.getFotoPerfil().isEmpty() &&
                !usuario.getFotoPerfil().contains("googleusercontent.com")) {
            cloudinaryService.eliminarImagen(usuario.getFotoPerfil());
            log.info("Foto de perfil eliminada de Cloudinary para usuario ID: {}", id);
        }

        // Eliminar todos los seguimientos relacionados (como seguidor y como seguido)
        seguimientoRepository.deleteBySeguidorIdUsuarioOrSeguidoIdUsuario(id, id);
        log.info("Seguimientos eliminados para usuario ID: {}", id);

        // ============================================
        // LLAMADAS A OTROS MICROSERVICIOS
        // ============================================

        // Eliminar datos del microservicio de Contenidos
        log.info("Eliminando datos del usuario en microservicio Contenidos...");
        contenidosClient.eliminarComprasUsuario(id);
        contenidosClient.eliminarFavoritosUsuario(id);
        contenidosClient.eliminarComentariosUsuario(id);

        // Eliminar datos del microservicio de Recomendaciones
        log.info("Eliminando preferencias en microservicio Recomendaciones...");
        recomendacionesClient.eliminarPreferenciasUsuario(id);

        // Revocar todos los tokens activos
        jwtService.revocarTodosLosTokensDelUsuario(id);

        // Desactivar cuenta (soft delete)
        usuario.setActivo(false);
        usuarioRepository.save(usuario);

        log.warn("Cuenta de usuario eliminada (soft delete). Usuario ID: {} eliminado por usuario ID: {}", id, authenticatedUserId);
    }

    /**
     * Solicita recuperación de contraseña enviando email con código de 6 dígitos.
     * ACTUALIZADO: Solo genera código, sin token UUID.
     * NO revela si el email existe para prevenir enumeración de usuarios.
     *
     * @param dto Contiene el email del usuario
     */
    @Transactional
    public void solicitarRecuperacionPassword(RecuperarPasswordDTO dto) {
        Optional<Usuario> usuarioOpt = usuarioRepository
                .findByEmailUsuario(dto.getEmailUsuario());

        // IMPORTANTE: Siempre responde igual (no revela si existe el email)
        if (usuarioOpt.isEmpty()) {
            log.info("Solicitud de recuperación para email no registrado: {}",
                    dto.getEmailUsuario());
            return; // No hacer nada, pero el controller responderá OK
        }

        Usuario usuario = usuarioOpt.get();

        // Si la cuenta está inactiva, no enviar email
        if (!usuario.isActivo()) {
            log.warn("Solicitud de recuperación para cuenta inactiva. Usuario ID: {}",
                    usuario.getIdUsuario());
            return; // No revelar que la cuenta existe pero está inactiva
        }

        // Generar código de 6 dígitos aleatorio
        String codigoVerificacion = generarCodigoAleatorio();
        LocalDateTime fechaExpiracion = LocalDateTime.now().plusHours(1); // 1 hora

        // Guardar SOLO el código (sin token UUID)
        usuario.setCodigoRecuperacion(codigoVerificacion);
        usuario.setFechaExpiracionTokenRecuperacion(fechaExpiracion);

        usuarioRepository.save(usuario);

        // Enviar email SOLO con el código
        try {
            emailService.enviarEmailRecuperacion(
                    usuario.getEmailUsuario(),
                    usuario.getNombreUsuario(),
                    codigoVerificacion
            );
            log.info("Email de recuperación enviado a: {}", usuario.getEmailUsuario());
        } catch (Exception e) {
            log.error("Error al enviar email de recuperación: {}", e.getMessage());
            // No lanzar excepción para no revelar información
        }
    }

    /**
     * Restablece la contraseña usando email + código recibidos por email.
     * ACTUALIZADO: Sin validación de token UUID, solo código.
     * Este endpoint es público (no requiere autenticación).
     *
     * @param dto Contiene email, código de 6 dígitos y la nueva contraseña
     * @throws InvalidPasswordResetTokenException Si el código es inválido o expiró
     * @throws UsuarioNotFoundException Si el usuario no existe
     */
    @Transactional
    public void restablecerPassword(RestablecerPasswordDTO dto) {
        // Buscar usuario por email
        Usuario usuario = usuarioRepository.findByEmailUsuario(dto.getEmailUsuario())
                .orElseThrow(() -> new InvalidPasswordResetTokenException(
                        "No se encontró una solicitud de recuperación activa para este email"
                ));

        // Validar que existe un código de recuperación activo
        if (usuario.getCodigoRecuperacion() == null || usuario.getCodigoRecuperacion().isEmpty()) {
            log.warn("No hay código de recuperación activo para usuario ID: {}", usuario.getIdUsuario());
            throw new InvalidPasswordResetTokenException(
                    "No hay ninguna solicitud de recuperación activa. Solicita un nuevo código"
            );
        }

        // Validar que el código de 6 dígitos coincida
        if (!dto.getCodigoVerificacion().equals(usuario.getCodigoRecuperacion())) {
            log.warn("Código de verificación incorrecto para usuario ID: {}", usuario.getIdUsuario());
            throw new InvalidPasswordResetTokenException(
                    "El código de verificación es incorrecto"
            );
        }

        // Validar que la cuenta esté activa
        if (!usuario.isActivo()) {
            log.warn("Intento de restablecer contraseña en cuenta inactiva. Usuario ID: {}",
                    usuario.getIdUsuario());
            throw new AccountInactiveException("La cuenta está inactiva. Contacta con soporte");
        }

        // Actualizar contraseña (hasheada)
        usuario.setPasswordUsuario(passwordEncoder.encode(dto.getNuevaPassword()));

        // Limpiar código y contadores de seguridad
        usuario.setCodigoRecuperacion(null);
        usuario.setFechaExpiracionTokenRecuperacion(null);

        usuarioRepository.save(usuario);

        // SEGURIDAD: Revocar todos los refresh tokens activos (logout global)
        jwtService.revocarTodosLosTokensDelUsuario(usuario.getIdUsuario());

        // Enviar email de confirmación del cambio (notificación de seguridad)
        try {
            emailService.enviarEmailConfirmacionCambioPassword(
                    usuario.getEmailUsuario(),
                    usuario.getNombreUsuario()
            );
        } catch (Exception e) {
            log.error("Error al enviar email de confirmación: {}", e.getMessage());
            // No fallar el proceso si el email no se envía
        }

        log.info("Contraseña restablecida exitosamente para usuario ID: {} usando código de 6 dígitos",
                usuario.getIdUsuario());
    }

    /**
     * Cambia la contraseña de un usuario autenticado.
     * Requiere la contraseña actual para mayor seguridad.
     *
     * @param id ID del usuario
     * @param dto Contiene la contraseña actual y la nueva
     * @param authenticatedUserId ID del usuario autenticado
     * @throws UsuarioNotFoundException Si el usuario no existe
     * @throws ForbiddenAccessException Si intenta cambiar la contraseña de otro usuario
     * @throws InvalidCredentialsException Si la contraseña actual es incorrecta
     */
    @Transactional
    public void cambiarPassword(Long id, CambiarPasswordDTO dto, Long authenticatedUserId) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));

        // Verificar que solo pueda cambiar su propia contraseña
        if (!usuario.getIdUsuario().equals(authenticatedUserId)) {
            log.warn("Usuario ID: {} intentó cambiar contraseña del usuario ID: {}",
                    authenticatedUserId, id);
            throw new ForbiddenAccessException(
                    "No tienes permiso para modificar este perfil"
            );
        }

        // Validar que la cuenta esté activa
        if (!usuario.isActivo()) {
            log.warn("Intento de cambiar contraseña en cuenta inactiva. Usuario ID: {}", id);
            throw new AccountInactiveException("La cuenta está inactiva. Contacta con soporte");
        }

        // Validar que no sea una cuenta solo de Google
        if (usuario.getPasswordUsuario() == null || usuario.getPasswordUsuario().isEmpty()) {
            log.warn("Intento de cambiar contraseña en cuenta de Google. Usuario ID: {}", id);
            throw new InvalidCredentialsException(
                    "Las cuentas de Google no pueden establecer contraseña. " +
                            "Usa la opción 'Olvidé mi contraseña' para crear una"
            );
        }

        // Validar contraseña actual
        if (!passwordEncoder.matches(dto.getPasswordActual(), usuario.getPasswordUsuario())) {
            log.warn("Contraseña actual incorrecta para usuario ID: {}", id);
            throw new InvalidCredentialsException("La contraseña actual es incorrecta");
        }

        // Validar que la nueva contraseña sea diferente a la actual
        if (passwordEncoder.matches(dto.getNuevaPassword(), usuario.getPasswordUsuario())) {
            log.warn("Usuario ID: {} intentó usar la misma contraseña", id);
            throw new InvalidDataException(
                    "La nueva contraseña debe ser diferente a la actual"
            );
        }

        // Actualizar contraseña
        usuario.setPasswordUsuario(passwordEncoder.encode(dto.getNuevaPassword()));
        usuarioRepository.save(usuario);

        // SEGURIDAD: Revocar todos los refresh tokens (logout global)
        jwtService.revocarTodosLosTokensDelUsuario(id);

        // Enviar email de notificación (alerta de seguridad)
        try {
            emailService.enviarEmailConfirmacionCambioPassword(
                    usuario.getEmailUsuario(),
                    usuario.getNombreUsuario()
            );
        } catch (Exception e) {
            log.error("Error al enviar email de confirmación: {}", e.getMessage());
            // No fallar el proceso si el email no se envía
        }

        log.info("Contraseña cambiada exitosamente para usuario ID: {}", id);
    }

    /**
     * Genera un código aleatorio de 6 dígitos.
     *
     * @return String con 6 dígitos numéricos
     */
    private String generarCodigoAleatorio() {
        Random random = new Random();
        int codigo = 100000 + random.nextInt(900000); // Rango: 100000-999999
        return String.valueOf(codigo);
    }

    /**
     * Convierte una entidad Usuario a UsuarioDTO.
     *
     * @param usuario Entidad Usuario
     * @return UsuarioDTO
     */
    private UsuarioDTO convertirAUsuarioDTO(Usuario usuario) {
        return UsuarioDTO.builder()
                .idUsuario(usuario.getIdUsuario())
                .emailUsuario(usuario.getEmailUsuario())
                .nombreUsuario(usuario.getNombreUsuario())
                .apellidosUsuario(usuario.getApellidosUsuario())
                .tipoUsuario(usuario.getTipoUsuario())
                .fotoPerfil(usuario.getFotoPerfil())
                .activo(usuario.isActivo())
                .permiteGoogle(usuario.isPermiteGoogle())
                .emailVerificado(usuario.isEmailVerificado())
                .build();
    }

    /**
     * Separa un nombre completo en nombre y apellidos.
     *
     * @param nombreCompleto Nombre completo
     * @return Array con [nombre, apellidos]
     */
    private String[] separarNombreCompleto(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.isEmpty()) {
            return new String[]{"Usuario", ""};
        }

        String[] partes = nombreCompleto.trim().split("\\s+", 2);
        String nombre = partes[0];
        String apellidos = partes.length > 1 ? partes[1] : "";

        return new String[]{nombre, apellidos};
    }
}