package com.ondra.users.exceptions;

import com.ondra.users.services.UsuarioService;

/**
 * Excepción lanzada cuando se intenta registrar un usuario con un email que ya existe en el sistema.
 *
 * <p>Esta excepción se utiliza durante el proceso de registro para indicar que el email
 * proporcionado ya está asociado a otra cuenta de usuario.</p>
 *
 * <p>Código de error API: EMAIL_ALREADY_EXISTS</p>
 * <p>Código HTTP: 400 Bad Request</p>
 *
 * @see UsuarioService#registrarUsuario
 */
public class EmailAlreadyExistsException extends RuntimeException {

    /**
     * Construye una nueva excepción de email duplicado con el mensaje especificado.
     *
     * @param message el mensaje de detalle que describe el error
     */
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}