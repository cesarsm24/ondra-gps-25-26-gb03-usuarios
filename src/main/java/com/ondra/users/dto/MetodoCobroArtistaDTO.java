package com.ondra.users.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO utilizado para representar la información de un método de cobro de un artista.
 *
 * Incluye campos generales obligatorios y campos específicos según el tipo de método.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetodoCobroArtistaDTO {

    /** Identificador del método de cobro. */
    private Long idMetodoCobro;

    /** Tipo de método de cobro (PAYPAL, BIZUM, TRANSFERENCIA). */
    private String tipo;

    /** Nombre del propietario del método de cobro. */
    private String propietario;

    /** Dirección asociada al método de cobro. */
    private String direccion;

    /** País asociado al método de cobro. */
    private String pais;

    /** Provincia asociada al método de cobro. */
    private String provincia;

    /** Código postal asociado al método de cobro. */
    private String codigoPostal;

    /** Email asociado a PAYPAL. Opcional según tipo de método de cobro. */
    private String emailPaypal;

    /** Teléfono asociado a BIZUM. Opcional según tipo de método de cobro. */
    private String telefonoBizum;

    /** IBAN asociado a TRANSFERENCIA. Opcional según tipo de método de cobro. */
    private String iban;

    /** Fecha de creación del registro, en formato ISO-8601. */
    private String fechaCreacion;

    /** Fecha de última actualización del registro, en formato ISO-8601. */
    private String fechaActualizacion;
}
