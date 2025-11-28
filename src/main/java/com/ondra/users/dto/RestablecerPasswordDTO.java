package com.ondra.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para restablecer la contraseña de un usuario mediante código de verificación.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestablecerPasswordDTO {

    /** Email del usuario que solicita el restablecimiento */
    @NotBlank(message = "El email es obligatorio")
    private String emailUsuario;

    /** Código de 6 dígitos recibido por email */
    @NotBlank(message = "El código de verificación es obligatorio")
    @Size(min = 6, max = 6, message = "El código debe tener 6 dígitos")
    private String codigoVerificacion;

    /** Nueva contraseña a establecer */
    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String nuevaPassword;
}