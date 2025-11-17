package com.ondra.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

// ==================== DTO DE EDICIÓN ====================
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MetodoCobroArtistaEditarDTO {

    @Size(min = 3, max = 200, message = "El propietario debe tener entre 3 y 200 caracteres")
    private String propietario;

    // Campo específico de PAYPAL
    @Email(message = "Email de PayPal inválido")
    @Size(max = 255)
    private String emailPaypal;

    // Campo específico de BIZUM
    @Pattern(regexp = "^(\\+34)?[6-9]\\d{8}$",
            message = "Teléfono Bizum inválido")
    private String telefonoBizum;

    // Campo específico de TRANSFERENCIA
    @Pattern(regexp = "^ES\\d{22}$",
            message = "IBAN inválido (formato: ES + 22 dígitos)")
    private String iban;
}