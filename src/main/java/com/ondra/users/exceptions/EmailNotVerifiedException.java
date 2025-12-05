package com.ondra.users.exceptions;

/**
 * Excepción lanzada cuando un usuario intenta iniciar sesión
 * sin haber verificado su dirección de correo electrónico.
 */
public class EmailNotVerifiedException extends RuntimeException {

    /**
     * Construye una nueva instancia de la excepción.
     *
     * @param mensaje descripción del motivo de la excepción
     */
    public EmailNotVerifiedException(String mensaje) {
        super(mensaje);
    }
}
