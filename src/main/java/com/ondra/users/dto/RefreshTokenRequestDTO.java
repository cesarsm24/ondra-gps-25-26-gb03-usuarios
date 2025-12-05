package com.ondra.users.dto;

import lombok.*;

/**
 * DTO utilizado para solicitar un nuevo access token a partir de un refresh token.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenRequestDTO {

    /** Refresh token proporcionado por el cliente. */
    private String refreshToken;
}
