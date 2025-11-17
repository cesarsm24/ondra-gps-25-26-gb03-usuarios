package com.ondra.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

// ==================== DTO DE CREACIÓN ====================
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MetodoPagoUsuarioCrearDTO {

    @NotBlank(message = "El método de pago es obligatorio")
    @Pattern(regexp = "TARJETA|PAYPAL|BIZUM|TRANSFERENCIA",
            message = "Método de pago no válido")
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

    // Campos específicos de TARJETA
    @Pattern(regexp = "^\\d{16}$", message = "Número de tarjeta inválido (16 dígitos)")
    private String numeroTarjeta;

    @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$",
            message = "Fecha de caducidad inválida (formato: MM/YY)")
    private String fechaCaducidad;

    @Pattern(regexp = "^\\d{3,4}$", message = "CVV inválido (3-4 dígitos)")
    private String cvv;

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