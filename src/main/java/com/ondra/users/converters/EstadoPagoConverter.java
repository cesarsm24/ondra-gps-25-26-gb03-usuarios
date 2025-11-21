package com.ondra.users.converters;

import com.ondra.users.models.enums.EstadoPago;
import jakarta.persistence.Converter;

/**
 * Converter JPA para la enumeración EstadoPago.
 *
 * <p>Conversión automática:</p>
 * <ul>
 *     <li>Java: PENDIENTE, COMPLETADO, FALLIDO</li>
 *     <li>Base de datos: pendiente, completado, fallido</li>
 * </ul>
 */
@Converter(autoApply = false)
public class EstadoPagoConverter extends LowercaseEnumConverter<EstadoPago> {

    /**
     * Constructor que inicializa el converter para EstadoPago.
     */
    public EstadoPagoConverter() {
        super(EstadoPago.class);
    }
}