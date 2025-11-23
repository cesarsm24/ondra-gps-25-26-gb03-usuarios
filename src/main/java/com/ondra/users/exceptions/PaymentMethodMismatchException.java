package com.ondra.users.exceptions;

import com.ondra.users.services.MetodoCobroArtistaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando se intenta operar sobre un método de pago
 * que no pertenece al artista o usuario especificado.
 *
 * <p>Esta excepción protege la integridad de los datos evitando que
 * se manipulen métodos de pago a través de IDs incorrectos o manipulados.</p>
 *
 * <p>Genera una respuesta HTTP 400 (Bad Request) con el código de error
 * <code>PAYMENT_METHOD_MISMATCH</code> según la especificación de la API.</p>
 *
 * @see MetodoCobroArtistaService
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PaymentMethodMismatchException extends RuntimeException {

    /**
     * Construye una nueva excepción con un mensaje predeterminado.
     */
    public PaymentMethodMismatchException() {
        super("El método de pago no pertenece a este artista");
    }

    /**
     * Construye una nueva excepción con un mensaje personalizado.
     *
     * @param message Descripción del error de correspondencia
     */
    public PaymentMethodMismatchException(String message) {
        super(message);
    }
}