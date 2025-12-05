package com.ondra.users.exceptions;

import com.ondra.users.services.ArtistaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción utilizada cuando un artista no existe en la base de datos.
 *
 * <p>Se lanza en operaciones de consulta, actualización o eliminación cuando
 * el identificador proporcionado no corresponde a ningún registro.</p>
 *
 * <p>Genera una respuesta HTTP 404.</p>
 *
 * @see ArtistaService
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ArtistaNotFoundException extends RuntimeException {

    /**
     * Crea la excepción indicando el identificador del artista no encontrado.
     *
     * @param id identificador del artista
     */
    public ArtistaNotFoundException(Long id) {
        super("El artista con ID " + id + " no fue encontrado");
    }

    /**
     * Crea la excepción con un mensaje personalizado.
     *
     * @param mensaje descripción del error
     */
    public ArtistaNotFoundException(String mensaje) {
        super(mensaje);
    }
}
