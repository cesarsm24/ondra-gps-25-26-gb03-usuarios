package com.ondra.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta que contiene los tokens renovados después de un refresh token.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenResponseDTO {

    /** Nuevo access token JWT. */
    private String accessToken;

    /** Refresh token (puede ser renovado o el mismo). */
    private String refreshToken;

    /** Tipo de token, por defecto "Bearer". */
    private String tipo = "Bearer";
}
