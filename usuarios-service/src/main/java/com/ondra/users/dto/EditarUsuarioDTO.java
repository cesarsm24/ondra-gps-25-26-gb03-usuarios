package com.ondra.users.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO utilizado para actualizar los datos del perfil de un usuario.
 *
 * <p>Incluye campos opcionales para modificar el nombre, apellidos y foto de perfil.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditarUsuarioDTO {

    /**
     * Nombre del usuario.
     *
     * <p>No puede estar vacío si se proporciona.</p>
     */
    @Size(min = 1, message = "El nombre no puede estar vacío")
    private String nombreUsuario;

    /**
     * Apellidos del usuario.
     *
     * <p>Campo opcional.</p>
     */
    private String apellidosUsuario;

    /**
     * URL o ruta de la foto de perfil del usuario.
     *
     * <p>Campo opcional.</p>
     */
    private String fotoPerfil;
}