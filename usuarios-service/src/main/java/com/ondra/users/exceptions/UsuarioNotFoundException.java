package com.ondra.users.exceptions;

/**
 * Excepción lanzada cuando no se encuentra un usuario en la base de datos.
 *
 * <p>Código de error asociado: USER_NOT_FOUND</p>
 * <p>Código HTTP: 404 NOT FOUND</p>
 */
public class UsuarioNotFoundException extends RuntimeException {

    /**
     * Constructor con ID del usuario.
     *
     * @param id ID del usuario no encontrado
     */
    public UsuarioNotFoundException(Long id) {
        super("El usuario con ID " + id + " no fue encontrado");
    }

    /**
     * Constructor con mensaje personalizado.
     *
     * @param mensaje Mensaje descriptivo del error
     */
    public UsuarioNotFoundException(String mensaje) {
        super(mensaje);
    }
}
