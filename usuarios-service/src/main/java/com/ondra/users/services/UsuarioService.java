package com.ondra.users.services;

import com.ondra.users.dto.*;
import com.ondra.users.security.JwtService;
import com.ondra.users.exceptions.*;
import com.ondra.users.models.dao.*;
import com.ondra.users.models.enums.TipoUsuario;
import com.ondra.users.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

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