package com.ondra.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO utilizado para crear un método de pago para un usuario.
 *
 * Contiene información obligatoria y campos específicos según el tipo de pago:
 * TARJETA, PAYPAL, BIZUM o TRANSFERENCIA.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MetodoPagoUsuarioCrearDTO {

    /** Tipo de método de pago. Obligatorio. Valores válidos: TARJETA, PAYPAL, BIZUM, TRANSFERENCIA. */
    @NotBlank(message = "El método de pago es obligatorio")
    @Pattern(regexp = "TARJETA|PAYPAL|BIZUM|TRANSFERENCIA", message = "Método de pago no válido")
    private String metodoPago;

    /** Nombre del propietario del método de pago. Obligatorio, entre 3 y 200 caracteres. */
    @NotBlank(message = "El propietario es obligatorio")
    @Size(min = 3, max = 200, message = "El propietario debe tener entre 3 y 200 caracteres")
    private String propietario;

    /** Dirección asociada al método de pago. Obligatoria, entre 5 y 300 caracteres. */
    @NotBlank(message = "La dirección es obligatoria")
    @Size(min = 5, max = 300, message = "La dirección debe tener entre 5 y 300 caracteres")
    private String direccion;

    /** País asociado al método de pago. Obligatorio, máximo 100 caracteres. */
    @NotBlank(message = "El país es obligatorio")
    @Size(max = 100)
    private String pais;

    /** Provincia asociada al método de pago. Obligatoria, máximo 100 caracteres. */
    @NotBlank(message = "La provincia es obligatoria")
    @Size(max = 100)
    private String provincia;

    /** Código postal asociado al método de pago. Obligatorio, formato 5 dígitos. */
    @NotBlank(message = "El código postal es obligatorio")
    @Pattern(regexp = "^\\d{5}$", message = "Código postal inválido (5 dígitos)")
    private String codigoPostal;

    /** Número de tarjeta (solo para TARJETA). Formato 16 dígitos. */
    @Pattern(regexp = "^\\d{16}$", message = "Número de tarjeta inválido (16 dígitos)")
    private String numeroTarjeta;

    /** Fecha de caducidad de la tarjeta (solo para TARJETA). Formato MM/YY. */
    @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$", message = "Fecha de caducidad inválida (formato: MM/YY)")
    private String fechaCaducidad;

    /** CVV de la tarjeta (solo para TARJETA). 3-4 dígitos. */
    @Pattern(regexp = "^\\d{3,4}$", message = "CVV inválido (3-4 dígitos)")
    private String cvv;

    /** Email asociado a PAYPAL. Opcional según tipo de pago. */
    @Email(message = "Email de PayPal inválido")
    @Size(max = 255)
    private String emailPaypal;

    /** Teléfono asociado a BIZUM. Opcional según tipo de pago. */
    @Pattern(regexp = "^(\\+34)?[6-9]\\d{8}$", message = "Teléfono Bizum inválido (formato: +34XXXXXXXXX)")
    private String telefonoBizum;

    /** IBAN asociado a TRANSFERENCIA. Opcional según tipo de pago. */
    @Pattern(regexp = "^ES\\d{22}$", message = "IBAN inválido (formato: ES + 22 dígitos)")
    private String iban;
}
