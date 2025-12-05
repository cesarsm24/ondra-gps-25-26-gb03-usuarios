package com.ondra.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO utilizado para crear una nueva red social asociada a un artista.
 *
 * Todos los campos son obligatorios.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedSocialCrearDTO {

    /**
     * Tipo de red social.
     *
     * Valores permitidos (case-insensitive):
     * instagram, x, facebook, youtube, tiktok, spotify, soundcloud
     */
    @NotBlank(message = "El tipo de red social es obligatorio")
    @Pattern(
            regexp = "^(instagram|x|facebook|youtube|tiktok|spotify|soundcloud|otra|INSTAGRAM|X|FACEBOOK|YOUTUBE|TIKTOK|SPOTIFY|SOUNDCLOUD)$",
            message = "Tipo de red social no válido. Valores permitidos: instagram, x, facebook, youtube, tiktok, spotify, soundcloud"
    )
    private String tipoRedSocial;

    /**
     * URL de la red social.
     *
     * Debe ser una URL válida que comience con http:// o https://
     * y tener entre 10 y 500 caracteres.
     */
    @NotBlank(message = "La URL de la red social es obligatoria")
    @Size(min = 10, max = 500, message = "La URL debe tener entre 10 y 500 caracteres")
    @Pattern(regexp = "^https?://.*", message = "La URL debe comenzar con http:// o https://")
    private String urlRedSocial;
}
