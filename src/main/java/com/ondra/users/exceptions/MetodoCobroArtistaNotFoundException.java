package com.ondra.users.exceptions;

import com.ondra.users.services.MetodoCobroArtistaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando no se encuentra un método de cobro de artista en la base de datos.
 *
 * <p>Esta excepción se utiliza en operaciones de búsqueda, actualización
 * y eliminación de métodos de cobro cuando el ID proporcionado no existe.</p>
 *
 * <p>Genera una respuesta HTTP 404 (Not Found) con el código de error
 * <code>PAYMENT_METHOD_NOT_FOUND</code> según la especificación de la API.</p>
 *
 * @see MetodoCobroArtistaService
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class MetodoCobroArtistaNotFoundException extends RuntimeException {

    /**
     * Construye una nueva excepción con un mensaje predeterminado
     * indicando el ID del método de cobro no encontrado.
     *
     * @param id El identificador del método de cobro que no fue encontrado
     */
    public MetodoCobroArtistaNotFoundException(Long id) {
        super("El método de cobro con ID " + id + " no fue encontrado");
    }
}