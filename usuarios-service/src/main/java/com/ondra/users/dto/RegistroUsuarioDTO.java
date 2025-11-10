package com.ondra.users.dto;

import com.ondra.users.models.enums.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO utilizado para registrar un nuevo usuario mediante email y contraseña.
 *
 * <p>Incluye información personal, credenciales y tipo de usuario.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroUsuarioDTO {

    /**
     * Correo electrónico del usuario.
     *
     * <p>Debe ser un email válido y es obligatorio para el registro.</p>
     */
    @Setter
    @Getter
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email es inválido")
    private String emailUsuario;

    /**
     * Contraseña del usuario.
     *
     * <p>Debe tener al menos 8 caracteres y es obligatoria.</p>
     */
    @Setter
    @Getter
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String passwordUsuario;

    /**
     * Nombre del usuario.
     *
     * <p>Campo obligatorio para el registro.</p>
     */
    @Getter
    @Setter
    @NotBlank(message = "El nombre es obligatorio")
    private String nombreUsuario;

    /**
     * Apellidos del usuario.
     *
     * <p>Campo obligatorio para el registro.</p>
     */
    @NotBlank(message = "Los apellidos son obligatorios")
    private String apellidosUsuario;

    /**
     * Tipo de usuario.
     *
     * <p>Indica si es un usuario normal, artista, etc. Campo obligatorio.</p>
     */
    @NotNull(message = "El tipo de usuario es obligatorio")
    private TipoUsuario tipoUsuario;
}