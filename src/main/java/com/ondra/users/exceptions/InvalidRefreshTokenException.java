package com.ondra.users.exceptions;

/**
 * Excepción lanzada cuando el token de actualización es inválido,
 * ha expirado o ha sido revocado.
 */
public class InvalidRefreshTokenException extends RuntimeException {

    /**
     * Construye una nueva instancia de la excepción.
     *
     * @param message descripción del motivo de la excepción
     */
    public InvalidRefreshTokenException(String message) {
        super(message);
    }

    /**
     * Construye una nueva instancia de la excepción con causa asociada.
     *
     * @param message descripción del motivo de la excepción
     * @param cause   causa original del error
     */
    public InvalidRefreshTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
