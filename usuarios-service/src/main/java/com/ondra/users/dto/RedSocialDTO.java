package com.ondra.users.dto;

import lombok.*;

/**
 * DTO que representa una red social asociada a un artista.
 *
 * <p>Incluye el identificador de la red social, el artista asociado,
 * el tipo de red social y la URL correspondiente.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedSocialDTO {

    /**
     * Identificador único de la red social.
     */
    private Long idRedSocial;

    /**
     * Identificador del artista al que pertenece la red social.
     */
    private Long idArtista;

    /**
     * Tipo de red social.
     *
     * <p>Ejemplos: <code>instagram</code>, <code>facebook</code>, <code>x</code>,
     * <code>tiktok</code>, <code>youtube</code>, <code>spotify</code>, <code>otra</code>.</p>
     */
    private String tipoRedSocial;

    /**
     * URL de la red social.
     */
    private String urlRedSocial;
}