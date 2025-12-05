package com.ondra.users.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO utilizado para actualizar los datos de un perfil artístico.
 *
 * Contiene campos opcionales para modificar el nombre artístico,
 * la biografía y la foto de perfil del artista.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditarArtistaDTO {

    /** Nombre artístico del artista. Opcional, entre 2 y 100 caracteres si se proporciona. */
    @Size(min = 2, max = 100, message = "El nombre artístico debe tener entre 2 y 100 caracteres")
    private String nombreArtistico;

    /** Biografía del artista. Opcional, máximo 1000 caracteres. */
    @Size(max = 1000, message = "La biografía no puede exceder los 1000 caracteres")
    private String biografiaArtistico;

    /** URL de la imagen de perfil del artista. Opcional. */
    private String fotoPerfilArtistico;
}
