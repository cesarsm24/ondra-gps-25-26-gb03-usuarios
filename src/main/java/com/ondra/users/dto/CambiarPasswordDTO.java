package com.ondra.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO utilizado para las operaciones de cambio de contraseña.
 *
 * Contiene la contraseña actual y la nueva contraseña que el usuario desea establecer.
 */
@Data
public class CambiarPasswordDTO {

    /** Contraseña actual del usuario. Obligatoria. */
    @NotBlank(message = "La contraseña actual es obligatoria")
    private String passwordActual;

    /** Nueva contraseña que se desea establecer. Obligatoria, mínimo 8 caracteres. */
    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String nuevaPassword;
}
