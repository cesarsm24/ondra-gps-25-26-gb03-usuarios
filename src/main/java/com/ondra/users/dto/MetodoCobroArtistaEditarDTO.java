package com.ondra.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO utilizado para actualizar un método de cobro de un artista.
 *
 * Contiene campos opcionales que se pueden modificar según el tipo de método:
 * PAYPAL, BIZUM o TRANSFERENCIA.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetodoCobroArtistaEditarDTO {

    /** Nombre del propietario del método de cobro. Opcional, entre 3 y 200 caracteres. */
    @Size(min = 3, max = 200, message = "El propietario debe tener entre 3 y 200 caracteres")
    private String propietario;

    /** Email asociado a PAYPAL. Opcional según tipo de método. */
    @Email(message = "Email de PayPal inválido")
    @Size(max = 255)
    private String emailPaypal;

    /** Teléfono asociado a BIZUM. Opcional según tipo de método. */
    @Pattern(regexp = "^(\\+34)?[6-9]\\d{8}$", message = "Teléfono Bizum inválido")
    private String telefonoBizum;

    /** IBAN asociado a TRANSFERENCIA. Opcional según tipo de método. */
    @Pattern(regexp = "^ES\\d{22}$", message = "IBAN inválido (formato: ES + 22 dígitos)")
    private String iban;
}
