package com.ondra.users.exceptions;

/**
 * Excepción lanzada cuando el token de restablecimiento de contraseña
 * no es válido o no cumple las condiciones necesarias.
 */
public class InvalidPasswordResetTokenException extends RuntimeException {

    /**
     * Construye una nueva instancia de la excepción.
     *
     * @param message descripción del motivo de la excepción
     */
    public InvalidPasswordResetTokenException(String message) {
        super(message);
    }
}
