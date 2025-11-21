package com.ondra.users.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO utilizado para actualizar una red social existente de un artista.
 *
 * Todos los campos son opcionales. Solo se actualizan los campos proporcionados.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedSocialEditarDTO {

    /**
     * Nuevo tipo de red social (opcional).
     *
     * Valores permitidos (case-insensitive):
     * instagram, x, facebook, youtube, tiktok, spotify, soundcloud, otra
     */
    @Pattern(
            regexp = "^(instagram|x|facebook|youtube|tiktok|spotify|soundcloud|otra|INSTAGRAM|X|FACEBOOK|YOUTUBE|TIKTOK|SPOTIFY|SOUNDCLOUD|OTRA)$",
            message = "Tipo de red social no válido. Valores permitidos: instagram, x, facebook, youtube, tiktok, spotify, soundcloud, otra"
    )
    private String tipoRedSocial;

    /**
     * Nueva URL de la red social (opcional).
     *
     * Debe ser una URL válida que comience con http:// o https://
     * y tener entre 10 y 500 caracteres.
     */
    @Size(min = 10, max = 500, message = "La URL debe tener entre 10 y 500 caracteres")
    @Pattern(regexp = "^https?://.*", message = "La URL debe comenzar con http:// o https://")
    private String urlRedSocial;
}
