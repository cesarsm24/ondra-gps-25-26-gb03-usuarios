package com.ondra.users.dto;

import lombok.*;

/**
 * DTO de respuesta para operaciones de autenticación.
 *
 * Contiene la información necesaria para que el cliente maneje la sesión:
 * <ul>
 *     <li><b>token:</b> Access token JWT de corta duración.</li>
 *     <li><b>refreshToken:</b> Token de larga duración para renovar el access token sin volver a iniciar sesión.</li>
 *     <li><b>usuario:</b> Datos del usuario autenticado encapsulados en {@link UsuarioDTO}.</li>
 *     <li><b>tipo:</b> Tipo de esquema de autorización, normalmente "Bearer".</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDTO {
    private String token;          // Access token (JWT corto)
    private String refreshToken;   // Refresh token (largo plazo)
    private UsuarioDTO usuario;    // Información del usuario autenticado
    private String tipo = "Bearer"; // Tipo de token
}