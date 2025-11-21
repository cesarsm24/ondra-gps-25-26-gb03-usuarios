package com.ondra.users.dto;

import com.ondra.users.models.enums.TipoUsuario;
import lombok.*;

/**
 * DTO que representa información básica de un usuario.
 *
 * Incluye datos de perfil y, si aplica, información artística resumida.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioBasicoDTO {

    /** Identificador único del usuario. */
    private Long idUsuario;

    /** Nombre del usuario. */
    private String nombreUsuario;

    /** Apellidos del usuario. */
    private String apellidosUsuario;

    /** URL o ruta de la foto de perfil del usuario. */
    private String fotoPerfil;

    /** Nombre artístico del usuario, solo si es artista. */
    private String nombreArtistico;

    /** Tipo de usuario (normal, artista, etc.). */
    private TipoUsuario tipoUsuario;

    /** Slug del usuario para URLs amigables. */
    private String slug;

    /** Slug artístico para URLs amigables, solo si es artista. */
    private String slugArtistico;
}
