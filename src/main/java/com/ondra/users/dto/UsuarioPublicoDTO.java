package com.ondra.users.dto;

import com.ondra.users.models.enums.TipoUsuario;
import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO que representa información pública de un usuario.
 *
 * <p>No incluye datos sensibles como email o contraseña.</p>
 * <p>Incluye datos de perfil y, si aplica, información artística básica.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioPublicoDTO {

    /** Identificador único del usuario. */
    private Long idUsuario;

    /** Nombre del usuario. */
    private String nombreUsuario;

    /** Slug del usuario para URLs amigables. */
    private String slug;

    /** Apellidos del usuario. */
    private String apellidosUsuario;

    /** Nombre artístico, solo si es artista. */
    private String nombreArtistico;

    /** URL o ruta de la foto de perfil del usuario. */
    private String fotoPerfil;

    /** Tipo de usuario (normal, artista, etc.). */
    private TipoUsuario tipoUsuario;

    /** Identificador del perfil artístico, solo si es artista. */
    private Long idArtista;

    /** Slug artístico, solo si es artista. */
    private String slugArtistico;

    /** Biografía artística, solo si es artista. */
    private String biografiaArtistico;

    /** Fecha y hora de registro del usuario. */
    private LocalDateTime fechaRegistro;

    /** URL o ruta de la foto de perfil artístico, solo si es artista. */
    private String fotoPerfilArtistico;
}
