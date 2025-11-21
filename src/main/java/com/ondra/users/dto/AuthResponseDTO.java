package com.ondra.users.dto;

import lombok.*;

/**
 * DTO de respuesta para operaciones de autenticación.
 *
 * Proporciona al cliente la información necesaria para gestionar la sesión:
 * <ul>
 *     <li><b>token:</b> Access token JWT de corta duración.</li>
 *     <li><b>refreshToken:</b> Token de larga duración para renovar el access token.</li>
 *     <li><b>usuario:</b> Datos del usuario autenticado encapsulados en {@link UsuarioDTO}.</li>
 *     <li><b>tipo:</b> Tipo de esquema de autorización, normalmente "Bearer".</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDTO {

    /** Access token JWT de corta duración. */
    private String token;

    /** Refresh token para renovar el access token sin reautenticación. */
    private String refreshToken;

    /** Información del usuario autenticado. */
    private UsuarioDTO usuario;

    /** Tipo de token utilizado, por defecto "Bearer". */
    private String tipo = "Bearer";
}
