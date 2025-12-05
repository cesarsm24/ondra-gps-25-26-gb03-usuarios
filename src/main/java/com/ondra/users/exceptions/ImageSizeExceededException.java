package com.ondra.users.exceptions;

/**
 * Excepción lanzada cuando el tamaño de la imagen supera el límite permitido.
 */
public class ImageSizeExceededException extends RuntimeException {

    /**
     * Construye una nueva instancia de la excepción.
     *
     * @param message descripción del motivo de la excepción
     */
    public ImageSizeExceededException(String message) {
        super(message);
    }
}
