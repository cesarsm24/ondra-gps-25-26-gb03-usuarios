package com.ondra.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para reenviar el correo de verificación de email.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReenviarVerificacionDTO {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String emailUsuario;

    private Long idUsuario;
}