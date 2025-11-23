package com.ondra.users.exceptions;

/**
 * Excepción lanzada cuando no se encuentra una relación de seguimiento
 * solicitada por el usuario.
 */
public class FollowNotFoundException extends RuntimeException {

    /**
     * Construye una nueva instancia de la excepción.
     *
     * @param message descripción del motivo de la excepción
     */
    public FollowNotFoundException(String message) {
        super(message);
    }
}
