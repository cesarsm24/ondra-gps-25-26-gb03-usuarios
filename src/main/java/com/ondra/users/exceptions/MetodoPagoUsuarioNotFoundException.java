package com.ondra.users.exceptions;

import com.ondra.users.services.MetodoPagoUsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando no se encuentra un método de pago de usuario en la base de datos.
 *
 * <p>Esta excepción se utiliza en operaciones de búsqueda, actualización
 * y eliminación de métodos de pago cuando el ID proporcionado no existe.</p>
 *
 * <p>Genera una respuesta HTTP 404 (Not Found) con el código de error
 * <code>PAYMENT_NOT_FOUND</code> según la especificación de la API.</p>
 *
 * @see MetodoPagoUsuarioService
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class MetodoPagoUsuarioNotFoundException extends RuntimeException {

    /**
     * Construye una nueva excepción con un mensaje predeterminado
     * indicando el ID del método de pago no encontrado.
     *
     * @param id El identificador del método de pago que no fue encontrado
     */
    public MetodoPagoUsuarioNotFoundException(Long id) {
        super("El método de pago con ID " + id + " no fue encontrado");
    }
}