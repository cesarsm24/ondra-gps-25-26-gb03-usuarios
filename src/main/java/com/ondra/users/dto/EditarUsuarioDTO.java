package com.ondra.users.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO utilizado para actualizar los datos del perfil de un usuario.
 *
 * Incluye campos opcionales para modificar el nombre, los apellidos
 * y la foto de perfil.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditarUsuarioDTO {

    /** Nombre del usuario. Opcional, no puede estar vacío si se proporciona. */
    @Size(min = 1, message = "El nombre no puede estar vacío")
    private String nombreUsuario;

    /** Apellidos del usuario. Campo opcional. */
    private String apellidosUsuario;

    /** URL de la foto de perfil del usuario. Campo opcional. */
    private String fotoPerfil;
}
