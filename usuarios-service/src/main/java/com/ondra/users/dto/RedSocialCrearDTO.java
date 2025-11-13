package com.ondra.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear una nueva red social de un artista.
 *
 * <p>Todos los campos son obligatorios.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedSocialCrearDTO {

    /**
     * Tipo de red social.
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
    @NotBlank(message = "El tipo de red social es obligatorio")
    @Pattern(
            regexp = "^(instagram|x|facebook|youtube|tiktok|spotify|soundcloud|otra|INSTAGRAM|X|FACEBOOK|YOUTUBE|TIKTOK|SPOTIFY|SOUNDCLOUD|OTRA)$",
            message = "Tipo de red social no válido. Valores permitidos: instagram, x, facebook, youtube, tiktok, spotify, soundcloud, otra"
    )
    private String tipoRedSocial;

    /**
     * URL de la red social.
     *
     * <p>Debe ser una URL válida que comience con http:// o https://</p>
     */
    @NotBlank(message = "La URL de la red social es obligatoria")
    @Size(min = 10, max = 500, message = "La URL debe tener entre 10 y 500 caracteres")
    @Pattern(
            regexp = "^https?://.*",
            message = "La URL debe comenzar con http:// o https://"
    )
    private String urlRedSocial;
}