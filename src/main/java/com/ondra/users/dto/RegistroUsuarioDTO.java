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
 * Incluye información personal, credenciales y tipo de usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroUsuarioDTO {

    /** Correo electrónico del usuario. Obligatorio y debe ser válido. */
    @Setter @Getter
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email es inválido")
    private String emailUsuario;

    /** Contraseña del usuario. Obligatoria y mínimo 8 caracteres. */
    @Setter @Getter
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String passwordUsuario;

    /** Nombre del usuario. Obligatorio. */
    @Getter @Setter
    @NotBlank(message = "El nombre es obligatorio")
    private String nombreUsuario;

    /** Apellidos del usuario. Obligatorio. */
    @NotBlank(message = "Los apellidos son obligatorios")
    private String apellidosUsuario;

    /** Tipo de usuario (normal, artista, etc.). Obligatorio. */
    @NotNull(message = "El tipo de usuario es obligatorio")
    private TipoUsuario tipoUsuario;
}
