package com.ondra.users.exceptions;

import com.ondra.users.services.ArtistaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando no se encuentra un artista en la base de datos.
 *
 * <p>Esta excepción se utiliza en operaciones de búsqueda, actualización
 * y eliminación de artistas cuando el ID proporcionado no existe.</p>
 *
 * <p>Genera una respuesta HTTP 404 (Not Found) con el código de error
 * <code>ARTIST_NOT_FOUND</code> según la especificación de la API.</p>
 *
 * @see ArtistaService
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ArtistaNotFoundException extends RuntimeException {

    public ArtistaNotFoundException(Long id) {
        super("El artista con ID " + id + " no fue encontrado");
    }

    public ArtistaNotFoundException(String mensaje) {
        super(mensaje);
    }
}