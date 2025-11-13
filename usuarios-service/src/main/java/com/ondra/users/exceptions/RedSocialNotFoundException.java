package com.ondra.users.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando no se encuentra una red social en la base de datos.
 *
 * <p>Esta excepción se utiliza en operaciones de búsqueda, actualización
 * y eliminación de redes sociales cuando el ID proporcionado no existe.</p>
 *
 * <p>Genera una respuesta HTTP 404 (Not Found) con el código de error
 * <code>SOCIAL_NETWORK_NOT_FOUND</code> según la especificación de la API.</p>
 *
 * @see com.ondra.users.services.RedSocialService
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class RedSocialNotFoundException extends RuntimeException {

    /**
     * Construye una nueva excepción con un mensaje predeterminado
     * indicando el ID de la red social no encontrada.
     *
     * @param id El identificador de la red social que no fue encontrada
     */
    public RedSocialNotFoundException(Long id) {
        super("La red social con ID " + id + " no fue encontrada");
    }
}