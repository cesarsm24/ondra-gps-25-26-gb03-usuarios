package com.ondra.users.exceptions;

/**
 * Excepción lanzada cuando un usuario intenta iniciar sesión sin haber verificado su email.
 */
public class EmailNotVerifiedException extends RuntimeException {
    public EmailNotVerifiedException(String mensaje) {
        super(mensaje);
    }
}
