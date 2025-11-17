package com.ondra.users.dto;

import lombok.*;

// ==================== DTO DE RESPUESTA ====================
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetodoPagoUsuarioDTO {
    private Long idMetodoPago;
    private String tipo;
    private String propietario;
    private String direccion;
    private String pais;
    private String provincia;
    private String codigoPostal;

    // Campos específicos (opcionales según tipo)
    private String numeroTarjeta;
    private String fechaCaducidad;
    private String cvv;
    private String emailPaypal;
    private String telefonoBizum;
    private String iban;

    private String fechaCreacion;
    private String fechaActualizacion;
}