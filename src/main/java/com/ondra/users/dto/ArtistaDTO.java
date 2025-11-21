package com.ondra.users.dto;

import lombok.*;
import java.util.List;

/**
 * DTO que representa el perfil público de un artista.
 * Contiene información artística básica, imagen de perfil,
 * estado de tendencia y enlaces a redes sociales.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtistaDTO {

    /** Identificador del artista. */
    private Long idArtista;

    /** Identificador del usuario asociado al perfil artístico. */
    private Long idUsuario;

    /** Nombre artístico mostrado públicamente. */
    private String nombreArtistico;

    /** Biografía o descripción del artista. */
    private String biografiaArtistico;

    /** URL de la imagen de perfil del artista. */
    private String fotoPerfilArtistico;

    /** Indica si el artista está marcado como tendencia. */
    private boolean esTendencia;

    /** Lista de redes sociales relacionadas con el artista. */
    private List<RedSocialDTO> redesSociales;

    /** Identificador legible utilizado para URLs. */
    private String slugArtistico;
}
