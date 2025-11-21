package com.ondra.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO utilizado para representar una respuesta exitosa de la API.
 *
 * Contiene información sobre el resultado de la operación, mensaje descriptivo,
 * código HTTP y marca temporal.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuccessfulResponseDTO {

    /** Código o identificador que indica éxito de la operación. */
    private String successful;

    /** Mensaje descriptivo sobre la operación realizada. */
    private String message;

    /** Código de estado HTTP asociado a la respuesta. */
    private int statusCode;

    /** Fecha y hora en que se generó la respuesta (formato ISO-8601). */
    private String timestamp;
}
