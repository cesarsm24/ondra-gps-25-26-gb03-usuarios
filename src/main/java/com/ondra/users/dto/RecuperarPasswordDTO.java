package com.ondra.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO utilizado para iniciar el proceso de recuperación de contraseña.
 *
 * Contiene el correo electrónico del usuario que desea recuperar su contraseña.
 */
@Data
public class RecuperarPasswordDTO {

    /** Correo electrónico del usuario. Obligatorio y debe ser válido. */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    private String emailUsuario;
}
