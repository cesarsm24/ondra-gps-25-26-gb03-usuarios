package com.ondra.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO utilizado para reenviar el correo de verificación de email.
 *
 * Contiene el correo electrónico del usuario y su identificador.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReenviarVerificacionDTO {

    /** Correo electrónico del usuario. Obligatorio y debe ser válido. */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String emailUsuario;

    /** Identificador del usuario al que se reenviará el correo. */
    private Long idUsuario;
}
