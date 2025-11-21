package com.ondra.users.models.dao;

import com.ondra.users.models.enums.TipoUsuario;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa un usuario del sistema.
 *
 * <p>Almacena información personal, credenciales, tipo de usuario, foto de perfil,
 * datos de registro y autenticación mediante Google OAuth. Incluye campos para
 * verificación de correo electrónico y recuperación de contraseña.</p>
 */
@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    /**
     * Identificador único del usuario.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    /**
     * Nombre del usuario.
     */
    @Column(nullable = false)
    private String nombreUsuario;

    /**
     * Apellidos del usuario.
     */
    private String apellidosUsuario;

    /**
     * Correo electrónico del usuario, único en el sistema.
     */
    @Column(nullable = false, unique = true)
    private String emailUsuario;

    /**
     * Contraseña del usuario almacenada como hash. Opcional si se utiliza Google OAuth.
     */
    @Column
    private String passwordUsuario;

    /**
     * Tipo de usuario del sistema.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoUsuario tipoUsuario;

    /**
     * Foto de perfil del usuario (URL o ruta).
     */
    @Builder.Default
    @Column(name = "foto_perfil")
    private String fotoPerfil = null;

    /**
     * Fecha y hora de registro del usuario.
     */
    @Column(nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    /**
     * Indica si la cuenta está activa.
     */
    @Column(nullable = false)
    private boolean activo = true;

    /**
     * Identificador único asignado por Google para autenticación OAuth.
     */
    @Column(unique = true)
    private String googleUid;

    /**
     * Indica si el usuario permite autenticación mediante Google OAuth.
     */
    @Column(nullable = false)
    private boolean permiteGoogle = true;

    /**
     * Indica si el correo electrónico del usuario ha sido verificado.
     */
    @Column(name = "email_verificado")
    private boolean emailVerificado = false;

    /**
     * Token generado para la verificación del correo electrónico.
     */
    @Column(name = "token_verificacion", unique = true)
    private String tokenVerificacion;

    /**
     * Fecha y hora de expiración del token de verificación.
     */
    @Column(name = "fecha_expiracion_token")
    private LocalDateTime fechaExpiracionToken;

    /**
     * Fecha y hora de expiración del token de recuperación de contraseña.
     */
    @Column(name = "fecha_expiracion_token_recuperacion")
    private LocalDateTime fechaExpiracionTokenRecuperacion;

    /**
     * Código de verificación para recuperación de contraseña.
     */
    @Column(name = "codigo_recuperacion", length = 6)
    private String codigoRecuperacion;

    /**
     * Identificador de URL amigable del usuario.
     */
    @Column(unique = true, length = 100)
    private String slug;

    /**
     * Indica si el usuario ha completado el proceso de onboarding inicial.
     */
    @Column(name = "onboarding_completado", nullable = false)
    private Boolean onboardingCompletado = false;

    /**
     * Perfil de artista asociado a este usuario. Solo se utiliza para usuarios de tipo ARTISTA.
     */
    @OneToOne(mappedBy = "usuario", fetch = FetchType.LAZY)
    private Artista artista;
}