package com.ondra.users.models.dao;

import com.ondra.users.models.enums.TipoUsuario;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa a un usuario del sistema.
 *
 * <p>Incluye información personal, credenciales, tipo de usuario, foto de perfil,
 * fecha de registro, estado activo y datos de integración con Google OAuth.</p>
 */
@Entity
@Table(name = "Usuarios")
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
     *
     * <p>Campo obligatorio.</p>
     */
    @Column(nullable = false)
    private String nombreUsuario;

    /**
     * Apellidos del usuario.
     *
     * <p>Campo opcional.</p>
     */
    private String apellidosUsuario;

    /**
     * Email del usuario.
     *
     * <p>Campo obligatorio y único.</p>
     */
    @Column(nullable = false, unique = true)
    private String emailUsuario;

    /**
     * Contraseña del usuario (almacenada como hash).
     *
     * <p>Campo opcional si el usuario usa Google OAuth.</p>
     */
    @Column
    private String passwordUsuario;

    /**
     * Tipo de usuario.
     *
     * <p>Se almacena como {@link TipoUsuario} (ENUM) y es obligatorio.</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoUsuario tipoUsuario;

    /**
     * Foto de perfil del usuario (URL o ruta).
     *
     * <p>Campo opcional.</p>
     */
    @Builder.Default
    @Column(name = "foto_perfil")
    private String fotoPerfil = null;

    /**
     * Fecha de registro del usuario.
     *
     * <p>Se inicializa automáticamente con la fecha actual.</p>
     */
    @Column(nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    /**
     * Indica si la cuenta está activa.
     *
     * <p>Por defecto es true.</p>
     */
    @Column(nullable = false)
    private boolean activo = true;

    /**
     * Identificador único de Google OAuth.
     *
     * <p>Campo único y opcional.</p>
     */
    @Column(unique = true)
    private String googleUid;

    /**
     * Indica si el usuario permite autenticación mediante Google OAuth.
     *
     * <p>Por defecto es true.</p>
     */
    @Column(nullable = false)
    private boolean permiteGoogle = true;

    // Añadir estos campos a Usuario.java
    @Column(name = "email_verificado")
    private boolean emailVerificado = false;

    @Column(name = "token_verificacion", unique = true)
    private String tokenVerificacion;

    @Column(name = "fecha_expiracion_token")
    private LocalDateTime fechaExpiracionToken;

    @Column(name = "fecha_expiracion_token_recuperacion")
    private LocalDateTime fechaExpiracionTokenRecuperacion;

    @Column(name = "codigo_recuperacion", length = 6)
    private String codigoRecuperacion;
}