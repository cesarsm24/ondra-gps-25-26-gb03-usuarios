package com.ondra.users.exceptions;

/**
 * Excepción lanzada cuando se produce un error al subir una imagen
 * al proveedor de almacenamiento.
 */
public class ImageUploadFailedException extends RuntimeException {

    /**
     * Construye una nueva instancia de la excepción.
     *
     * @param message descripción del motivo de la excepción
     */
    public ImageUploadFailedException(String message) {
        super(message);
    }

    /**
     * Construye una nueva instancia de la excepción con causa asociada.
     *
     * @param message descripción del motivo de la excepción
     * @param cause   causa original del error
     */
    public ImageUploadFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
