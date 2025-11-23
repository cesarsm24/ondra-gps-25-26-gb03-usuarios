package com.ondra.users.exceptions;

/**
 * Excepción lanzada cuando se intenta registrar una relación de seguimiento
 * no válida según las reglas del sistema.
 */
public class InvalidFollowException extends RuntimeException {

    /**
     * Construye una nueva instancia de la excepción.
     *
     * @param message descripción del motivo de la excepción
     */
    public InvalidFollowException(String message) {
        super(message);
    }
}
