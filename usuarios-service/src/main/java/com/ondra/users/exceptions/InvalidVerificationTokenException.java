package com.ondra.users.exceptions;

public class InvalidVerificationTokenException extends RuntimeException {
    public InvalidVerificationTokenException(String mensaje) {
        super(mensaje);
    }
}
