package com.ondra.users.dto;

import com.ondra.users.models.enums.TipoUsuario;
import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO que representa información completa de un usuario.
 *
 * Incluye datos de perfil, estado de la cuenta y, si aplica, información artística.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioDTO {

    /** Identificador único del usuario. */
    private Long idUsuario;

    /** Correo electrónico del usuario. */
    private String emailUsuario;

    /** Nombre del usuario. */
    private String nombreUsuario;

    /** Apellidos del usuario. */
    private String apellidosUsuario;

    /** Tipo de usuario (normal, artista, etc.). */
    private TipoUsuario tipoUsuario;

    /** URL o ruta de la foto de perfil del usuario. */
    private String fotoPerfil;

    /** Indica si la cuenta del usuario está activa. */
    private boolean activo;

    /** Indica si el usuario puede autenticarse mediante Google OAuth. */
    private boolean permiteGoogle;

    /** Indica si el email del usuario ha sido verificado. */
    private boolean emailVerificado;

    /** Fecha y hora de registro del usuario. */
    private LocalDateTime fechaRegistro;

    /** Indica si el usuario ha completado el onboarding inicial. */
    private Boolean onboardingCompletado;

    /** Slug del usuario para URLs amigables. */
    private String slug;

    // Campos relacionados únicamente con usuarios de tipo ARTISTA

    /** Identificador del perfil artístico, solo si es artista. */
    private Long idArtista;

    /** Nombre artístico del usuario, solo si es artista. */
    private String nombreArtistico;

    /** Biografía artística del usuario, solo si es artista. */
    private String biografiaArtistico;

    /** Slug artístico para URLs amigables, solo si es artista. */
    private String slugArtistico;

    /** URL o ruta de la foto de perfil artístico, solo si es artista. */
    private String fotoPerfilArtistico;
}
