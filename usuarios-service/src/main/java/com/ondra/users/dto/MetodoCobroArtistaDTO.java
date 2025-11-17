package com.ondra.users.dto;

import jakarta.validation.constraints.*;
import lombok.*;

// ==================== DTO DE RESPUESTA ====================
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetodoCobroArtistaDTO {
    private Long idMetodoCobro;
    private String tipo;
    private String propietario;
    private String direccion;
    private String pais;
    private String provincia;
    private String codigoPostal;

    // Campos específicos (opcionales según tipo)
    private String emailPaypal;
    private String telefonoBizum;
    private String iban;

    private String fechaCreacion;
    private String fechaActualizacion;
}