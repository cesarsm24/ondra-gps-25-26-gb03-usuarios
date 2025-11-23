package com.ondra.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO utilizado para respuestas de operaciones de subida de imágenes.
 *
 * Contiene la URL pública de la imagen subida y un mensaje descriptivo.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImagenResponseDTO {

    /** URL pública de la imagen en Cloudinary. */
    private String url;

    /** Mensaje descriptivo de la operación. */
    private String mensaje;
}
