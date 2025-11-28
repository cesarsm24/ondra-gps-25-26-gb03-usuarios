package com.ondra.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa los datos básicos de un usuario para su visualización.
 *
 * <p>Proporciona el nombre completo, el tipo de usuario, el identificador
 * público y la URL de la imagen asociada.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatosUsuarioDTO {

    /**
     * Nombre mostrado.
     * Para artistas corresponde al nombre artístico.
     * Para usuarios corresponde al nombre y apellidos.
     */
    private String nombreCompleto;

    /**
     * Indica si el usuario es normal o artista.
     */
    private String tipoUsuario;

    /**
     * Identificador público del usuario.
     */
    private String slug;

    /**
     * URL de la imagen asociada al usuario.
     */
    private String urlFoto;
}
