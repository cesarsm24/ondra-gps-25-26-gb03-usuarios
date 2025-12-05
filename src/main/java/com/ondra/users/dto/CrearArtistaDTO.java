package com.ondra.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO utilizado para crear un nuevo perfil artístico.
 *
 * Contiene la información obligatoria del artista: nombre artístico,
 * biografía y foto de perfil.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearArtistaDTO {

    /** Nombre artístico del artista. Obligatorio, entre 2 y 50 caracteres. */
    @NotBlank(message = "El nombre artístico es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre artístico debe tener entre 2 y 50 caracteres")
    private String nombreArtistico;

    /** Biografía del artista. Obligatoria, entre 50 y 500 caracteres. */
    @NotBlank(message = "La biografía es obligatoria")
    @Size(min = 50, max = 500, message = "La biografía debe tener entre 50 y 500 caracteres")
    private String biografiaArtistico;

    /** URL de la foto de perfil del artista. Obligatoria. */
    @NotBlank(message = "La foto de perfil es obligatoria")
    private String fotoPerfilArtistico;
}
