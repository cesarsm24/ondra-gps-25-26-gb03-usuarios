package com.ondra.users.exceptions;

/**
 * Excepción lanzada cuando se produce un error al eliminar una imagen
 * del proveedor de almacenamiento.
 */
public class ImageDeletionFailedException extends RuntimeException {

    /**
     * Construye una nueva instancia de la excepción.
     *
     * @param message descripción del motivo de la excepción
     */
    public ImageDeletionFailedException(String message) {
        super(message);
    }

    /**
     * Construye una nueva instancia de la excepción con causa asociada.
     *
     * @param message descripción del motivo de la excepción
     * @param cause   causa original del error
     */
    public ImageDeletionFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
