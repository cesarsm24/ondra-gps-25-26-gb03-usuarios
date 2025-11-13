package com.ondra.users.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando se intenta operar sobre una red social
 * que no pertenece al artista especificado.
 *
 * <p>Esta excepción protege la integridad de los datos evitando que
 * se manipulen redes sociales a través de IDs incorrectos o manipulados.</p>
 *
 * <p>Genera una respuesta HTTP 400 (Bad Request) con el código de error
 * <code>INVALID_DATA</code> según la especificación de la API.</p>
 *
 * @see com.ondra.users.services.RedSocialService
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SocialNetworkMismatchException extends RuntimeException {

    /**
     * Construye una nueva excepción con un mensaje predeterminado.
     */
    public SocialNetworkMismatchException() {
        super("La red social no pertenece a este artista");
    }
}