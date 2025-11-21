package com.ondra.users.exceptions;

/**
 * Excepción lanzada cuando una operación requiere un archivo
 * y la petición no incluye ninguno.
 */
public class NoFileProvidedException extends RuntimeException {

    /**
     * Construye una nueva instancia de la excepción.
     *
     * @param message descripción del motivo de la excepción
     */
    public NoFileProvidedException(String message) {
        super(message);
    }
}
