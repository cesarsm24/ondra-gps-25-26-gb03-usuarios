package com.ondra.users.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO utilizado para la autenticación de un usuario mediante Google OAuth.
 *
 * Contiene el token proporcionado por Google que permite verificar la identidad
 * del usuario y generar un JWT en el sistema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginGoogleDTO {

    /** Token de Google obtenido tras la autenticación en el cliente. Obligatorio. */
    @NotBlank(message = "El token de Google es obligatorio")
    private String idToken;
}
