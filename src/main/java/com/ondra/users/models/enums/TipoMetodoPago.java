package com.ondra.users.models.enums;

/**
 * Enumeración de los métodos de pago soportados en la plataforma.
 */
public enum TipoMetodoPago {
    /**
     * Pago con tarjeta de crédito o débito
     */
    TARJETA,

    /**
     * Pago mediante PayPal
     */
    PAYPAL,

    /**
     * Transferencia bancaria
     */
    TRANSFERENCIA,

    /**
     * Pago mediante Bizum
     */
    BIZUM
}