package com.ondra.users.dto;

import lombok.*;

/**
 * DTO utilizado para representar la información de un método de pago de un usuario.
 *
 * Incluye campos generales y opcionales según el tipo de pago:
 * TARJETA, PAYPAL, BIZUM o TRANSFERENCIA.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetodoPagoUsuarioDTO {

    /** Identificador del método de pago. */
    private Long idMetodoPago;

    /** Tipo de método de pago. */
    private String tipo;

    /** Nombre del propietario del método de pago. */
    private String propietario;

    /** Dirección asociada al método de pago. */
    private String direccion;

    /** País asociado al método de pago. */
    private String pais;

    /** Provincia asociada al método de pago. */
    private String provincia;

    /** Código postal asociado al método de pago. */
    private String codigoPostal;

    /** Número de tarjeta (solo para TARJETA). */
    private String numeroTarjeta;

    /** Fecha de caducidad de la tarjeta (solo para TARJETA). */
    private String fechaCaducidad;

    /** CVV de la tarjeta (solo para TARJETA). */
    private String cvv;

    /** Email asociado a PAYPAL. Opcional según tipo de pago. */
    private String emailPaypal;

    /** Teléfono asociado a BIZUM. Opcional según tipo de pago. */
    private String telefonoBizum;

    /** IBAN asociado a TRANSFERENCIA. Opcional según tipo de pago. */
    private String iban;

    /** Fecha de creación del registro. */
    private String fechaCreacion;

    /** Fecha de última actualización del registro. */
    private String fechaActualizacion;
}
