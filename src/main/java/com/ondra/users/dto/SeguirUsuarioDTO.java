package com.ondra.users.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO utilizado para seguir a otro usuario.
 *
 * Contiene únicamente el identificador del usuario a seguir.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeguirUsuarioDTO {

    /** ID del usuario que se desea seguir. Obligatorio. */
    @NotNull(message = "El ID del usuario a seguir es obligatorio")
    private Long idUsuarioASeguir;
}
