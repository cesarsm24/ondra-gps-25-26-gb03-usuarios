package com.ondra.users.exceptions;

/**
 * Excepción lanzada cuando un token de verificación no es válido
 * o no cumple las condiciones requeridas.
 */
public class InvalidVerificationTokenException extends RuntimeException {

    /**
     * Construye una nueva instancia de la excepción.
     *
     * @param mensaje descripción del motivo de la excepción
     */
    public InvalidVerificationTokenException(String mensaje) {
        super(mensaje);
    }
}
