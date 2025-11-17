package com.ondra.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MetodoCobroArtistaCrearDTO {

    @NotBlank(message = "El método de cobro es obligatorio")
    @Pattern(regexp = "PAYPAL|BIZUM|TRANSFERENCIA",
            message = "Método de cobro no válido (solo PAYPAL, BIZUM o TRANSFERENCIA)")
    private String metodoPago;

    @NotBlank(message = "El propietario es obligatorio")
    @Size(min = 3, max = 200, message = "El propietario debe tener entre 3 y 200 caracteres")
    private String propietario;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(min = 5, max = 300, message = "La dirección debe tener entre 5 y 300 caracteres")
    private String direccion;

    @NotBlank(message = "El país es obligatorio")
    @Size(max = 100)
    private String pais;

    @NotBlank(message = "La provincia es obligatoria")
    @Size(max = 100)
    private String provincia;

    @NotBlank(message = "El código postal es obligatorio")
    @Pattern(regexp = "^\\d{5}$", message = "Código postal inválido (5 dígitos)")
    private String codigoPostal;

    // Campo específico de PAYPAL
    @Email(message = "Email de PayPal inválido")
    @Size(max = 255)
    private String emailPaypal;

    // Campo específico de BIZUM
    @Pattern(regexp = "^(\\+34)?[6-9]\\d{8}$",
            message = "Teléfono Bizum inválido (formato: +34XXXXXXXXX)")
    private String telefonoBizum;

    // Campo específico de TRANSFERENCIA
    @Pattern(regexp = "^ES\\d{22}$",
            message = "IBAN inválido (formato: ES + 22 dígitos)")
    private String iban;
}