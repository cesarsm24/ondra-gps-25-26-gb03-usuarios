package com.ondra.users.exceptions;

/**
 * Excepción lanzada cuando el archivo de imagen no cumple con el formato permitido.
 */
public class InvalidImageFormatException extends RuntimeException {

    /**
     * Construye una nueva instancia de la excepción.
     *
     * @param message descripción del motivo de la excepción
     */
    public InvalidImageFormatException(String message) {
        super(message);
    }
}
