package com.ondra.users.converters;

import com.ondra.users.models.enums.TipoMetodoPago;
import jakarta.persistence.Converter;

/**
 * Converter JPA para la enumeración TipoMetodoPago.
 *
 * <p>Conversión automática:</p>
 * <ul>
 *     <li>Java: TARJETA, PAYPAL, TRANSFERENCIA, BIZUM, OTRO</li>
 *     <li>Base de datos: tarjeta, paypal, transferencia, bizum, otro</li>
 * </ul>
 */
@Converter(autoApply = false)
public class MetodoPagoConverter extends LowercaseEnumConverter<TipoMetodoPago> {

    /**
     * Constructor que inicializa el converter para TipoMetodoPago.
     */
    public MetodoPagoConverter() {
        super(TipoMetodoPago.class);
    }
}