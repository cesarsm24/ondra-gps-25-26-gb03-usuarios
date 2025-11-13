package com.ondra.users.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando se intenta crear una red social duplicada
 * del mismo tipo para un artista.
 *
 * <p>Esta excepción valida que un artista no tenga múltiples redes sociales
 * del mismo tipo (excepto para el tipo "OTRA").</p>
 *
 * <p>Genera una respuesta HTTP 400 (Bad Request) con el código de error
 * <code>INVALID_DATA</code> según la especificación de la API.</p>
 *
 * @see com.ondra.users.services.RedSocialService
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DuplicateSocialNetworkException extends RuntimeException {

    /**
     * Construye una nueva excepción con un mensaje indicando el tipo duplicado.
     *
     * @param tipo El tipo de red social que está duplicado
     */
    public DuplicateSocialNetworkException(String tipo) {
        super("Ya existe una red social de tipo " + tipo + " para este artista");
    }
}