package com.ondra.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO utilizado para actualizar un método de pago de un usuario.
 *
 * Todos los campos son opcionales. Solo se actualizan los que se envíen.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetodoPagoUsuarioEditarDTO {

    /** Nombre del propietario del método de pago. Opcional, entre 3 y 200 caracteres. */
    @Size(min = 3, max = 200, message = "El propietario debe tener entre 3 y 200 caracteres")
    private String propietario;

    /** Número de tarjeta (solo para TARJETA). Opcional, formato 16 dígitos. */
    @Pattern(regexp = "^\\d{16}$", message = "Número de tarjeta inválido (16 dígitos)")
    private String numeroTarjeta;

    /** Fecha de caducidad de la tarjeta (solo para TARJETA). Opcional, formato MM/YY. */
    @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$", message = "Fecha de caducidad inválida (formato: MM/YY)")
    private String fechaCaducidad;

    /** CVV de la tarjeta (solo para TARJETA). Opcional, 3-4 dígitos. */
    @Pattern(regexp = "^\\d{3,4}$", message = "CVV inválido (3-4 dígitos)")
    private String cvv;

    /** Email asociado a PAYPAL. Opcional. */
    @Email(message = "Email de PayPal inválido")
    @Size(max = 255)
    private String emailPaypal;

    /** Teléfono asociado a BIZUM. Opcional. */
    @Pattern(regexp = "^(\\+34)?[6-9]\\d{8}$", message = "Teléfono Bizum inválido")
    private String telefonoBizum;

    /** IBAN asociado a TRANSFERENCIA. Opcional, formato ES + 22 dígitos. */
    @Pattern(regexp = "^ES\\d{22}$", message = "IBAN inválido (formato: ES + 22 dígitos)")
    private String iban;
}
