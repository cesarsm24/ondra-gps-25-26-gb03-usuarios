package com.ondra.users.exceptions;

/**
 * Excepción lanzada cuando se intenta registrar una relación de seguimiento
 * que ya existe en el sistema.
 */
public class DuplicateFollowException extends RuntimeException {

    /**
     * Construye una nueva instancia de la excepción.
     *
     * @param message descripción del motivo de la excepción
     */
    public DuplicateFollowException(String message) {
        super(message);
    }
}
