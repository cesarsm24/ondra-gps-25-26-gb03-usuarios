package com.ondra.users.exceptions;

/**
 * Excepción lanzada cuando el identificador único solicitado
 * ya está registrado en el sistema.
 */
public class SlugAlreadyExistsException extends RuntimeException {

    /**
     * Construye una nueva instancia de la excepción.
     *
     * @param message descripción del motivo de la excepción
     */
    public SlugAlreadyExistsException(String message) {
        super(message);
    }
}
