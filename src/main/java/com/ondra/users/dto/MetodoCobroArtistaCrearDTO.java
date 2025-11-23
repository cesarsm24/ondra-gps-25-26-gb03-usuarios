package com.ondra.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO utilizado para crear un método de cobro para un artista.
 *
 * Contiene la información obligatoria según el tipo de método de cobro:
 * PAYPAL, BIZUM o TRANSFERENCIA.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MetodoCobroArtistaCrearDTO {

    /** Tipo de método de cobro. Obligatorio. Valores válidos: PAYPAL, BIZUM, TRANSFERENCIA. */
    @NotBlank(message = "El método de cobro es obligatorio")
    @Pattern(regexp = "PAYPAL|BIZUM|TRANSFERENCIA",
            message = "Método de cobro no válido (solo PAYPAL, BIZUM o TRANSFERENCIA)")
    private String metodoPago;

    /** Nombre del propietario del método de cobro. Obligatorio, entre 3 y 200 caracteres. */
    @NotBlank(message = "El propietario es obligatorio")
    @Size(min = 3, max = 200, message = "El propietario debe tener entre 3 y 200 caracteres")
    private String propietario;

    /** Dirección asociada al método de cobro. Obligatoria, entre 5 y 300 caracteres. */
    @NotBlank(message = "La dirección es obligatoria")
    @Size(min = 5, max = 300, message = "La dirección debe tener entre 5 y 300 caracteres")
    private String direccion;

    /** País asociado al método de cobro. Obligatorio, máximo 100 caracteres. */
    @NotBlank(message = "El país es obligatorio")
    @Size(max = 100)
    private String pais;

    /** Provincia asociada al método de cobro. Obligatoria, máximo 100 caracteres. */
    @NotBlank(message = "La provincia es obligatoria")
    @Size(max = 100)
    private String provincia;

    /** Código postal asociado al método de cobro. Obligatorio, formato 5 dígitos. */
    @NotBlank(message = "El código postal es obligatorio")
    @Pattern(regexp = "^\\d{5}$", message = "Código postal inválido (5 dígitos)")
    private String codigoPostal;

    /** Email asociado a PAYPAL. Opcional según tipo de método de cobro. */
    @Email(message = "Email de PayPal inválido")
    @Size(max = 255)
    private String emailPaypal;

    /** Teléfono asociado a BIZUM. Opcional según tipo de método de cobro. */
    @Pattern(regexp = "^(\\+34)?[6-9]\\d{8}$",
            message = "Teléfono Bizum inválido (formato: +34XXXXXXXXX)")
    private String telefonoBizum;

    /** IBAN asociado a TRANSFERENCIA. Opcional según tipo de método de cobro. */
    @Pattern(regexp = "^ES\\d{22}$",
            message = "IBAN inválido (formato: ES + 22 dígitos)")
    private String iban;
}
