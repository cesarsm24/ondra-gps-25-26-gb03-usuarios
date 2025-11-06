package com.ondra.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO utilizado para representar errores en las respuestas de la API.
 *
 * <p>Incluye información sobre el tipo de error, mensaje, código de estado HTTP
 * y la marca de tiempo en que ocurrió.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorDTO {

    /**
     * Código de error o tipo de error.
     */
    private String error;

    /**
     * Mensaje descriptivo del error.
     */
    private String message;

    /**
     * Código de estado HTTP asociado al error.
     */
    private int statusCode;

    /**
     * Fecha y hora en que ocurrió el error, en formato ISO-8601.
     */
    private String timestamp;
}