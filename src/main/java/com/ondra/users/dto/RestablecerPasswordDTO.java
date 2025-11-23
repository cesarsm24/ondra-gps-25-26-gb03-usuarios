package com.ondra.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO utilizado para restablecer la contraseña de un usuario.
 *
 * Contiene el email del usuario, el código de verificación recibido
 * y la nueva contraseña.
 */
@Data
public class RestablecerPasswordDTO {

    /** Correo electrónico del usuario. Obligatorio y válido. */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    private String emailUsuario;

    /** Código de verificación de 6 dígitos enviado al usuario. */
    @NotBlank(message = "El código de verificación es obligatorio")
    @Pattern(regexp = "\\d{6}", message = "El código debe tener 6 dígitos")
    private String codigoVerificacion;

    /** Nueva contraseña del usuario. Obligatoria y mínimo 8 caracteres. */
    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String nuevaPassword;
}
