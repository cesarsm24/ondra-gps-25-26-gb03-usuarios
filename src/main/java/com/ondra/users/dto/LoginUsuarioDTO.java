package com.ondra.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO utilizado para la autenticación de un usuario mediante email y contraseña.
 *
 * Contiene las credenciales necesarias para generar un token JWT en el sistema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginUsuarioDTO {

    /** Correo electrónico del usuario. Obligatorio y debe ser válido. */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email es inválido")
    private String emailUsuario;

    /** Contraseña del usuario. Obligatoria para la autenticación. */
    @NotBlank(message = "La contraseña es obligatoria")
    private String passwordUsuario;
}
