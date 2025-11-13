package com.ondra.users.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para editar una red social existente de un artista.
 *
 * <p>Todos los campos son opcionales. Solo se actualizan los campos que se envíen.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedSocialEditarDTO {

    /**
     * Nuevo tipo de red social (opcional).
     *
     * <p>Valores permitidos (case-insensitive):</p>
     * <ul>
     *   <li>instagram</li>
     *   <li>x</li>
     *   <li>facebook</li>
     *   <li>youtube</li>
     *   <li>tiktok</li>
     *   <li>spotify</li>
     *   <li>soundcloud</li>
     *   <li>otra</li>
     * </ul>
     */
    @Pattern(
            regexp = "^(instagram|x|facebook|youtube|tiktok|spotify|soundcloud|otra|INSTAGRAM|X|FACEBOOK|YOUTUBE|TIKTOK|SPOTIFY|SOUNDCLOUD|OTRA)$",
            message = "Tipo de red social no válido. Valores permitidos: instagram, x, facebook, youtube, tiktok, spotify, soundcloud, otra"
    )
    private String tipoRedSocial;

    /**
     * Nueva URL de la red social (opcional).
     *
     * <p>Debe ser una URL válida que comience con http:// o https://</p>
     */
    @Size(min = 10, max = 500, message = "La URL debe tener entre 10 y 500 caracteres")
    @Pattern(
            regexp = "^https?://.*",
            message = "La URL debe comenzar con http:// o https://"
    )
    private String urlRedSocial;
}