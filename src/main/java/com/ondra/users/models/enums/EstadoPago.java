package com.ondra.users.models.enums;

/**
 * Enumeración de los estados posibles de un método de pago.
 */
public enum EstadoPago {
    /**
     * Método de pago pendiente de verificación
     */
    PENDIENTE,

    /**
     * Método de pago verificado y activo
     */
    COMPLETADO,

    /**
     * Método de pago fallido o inválido
     */
    FALLIDO
}