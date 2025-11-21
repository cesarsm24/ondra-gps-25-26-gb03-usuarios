package com.ondra.users.exceptions;

import com.ondra.users.services.MetodoCobroArtistaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando se intenta utilizar un método de pago inválido
 * o no permitido para el tipo de usuario.
 *
 * <p>Se utiliza para validar que los métodos de pago sean apropiados
 * según el contexto (artista vs usuario) y que cumplan con las reglas
 * de negocio establecidas.</p>
 *
 * <p>Genera una respuesta HTTP 400 (Bad Request) con el código de error
 * <code>INVALID_PAYMENT_METHOD</code> según la especificación de la API.</p>
 *
 * <p>Ejemplos de uso:</p>
 * <ul>
 *   <li>Artistas intentando usar tarjeta como método de cobro</li>
 *   <li>Métodos de pago no reconocidos por el sistema</li>
 *   <li>Combinaciones inválidas de método y estado de pago</li>
 * </ul>
 *
 * @see MetodoCobroArtistaService
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidPaymentMethodException extends RuntimeException {

    /**
     * Construye una nueva excepción con un mensaje personalizado
     * describiendo el error del método de pago.
     *
     * @param message Descripción del error del método de pago
     */
    public InvalidPaymentMethodException(String message) {
        super(message);
    }
}