package com.ondra.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO utilizado para actualizar un método de pago de un usuario.
 *
 * <p>Todos los campos son opcionales. Solo se actualizan los que se envíen.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MetodoPagoUsuarioEditarDTO {

    @Size(min = 3, max = 200, message = "El propietario debe tener entre 3 y 200 caracteres")
    private String propietario;

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
            message = "Teléfono Bizum inválido")
    private String telefonoBizum;

    // Campo específico de TRANSFERENCIA
    @Pattern(regexp = "^ES\\d{22}$",
            message = "IBAN inválido (formato: ES + 22 dígitos)")
    private String iban;
}