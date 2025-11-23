package com.ondra.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para obtener el nombre de un usuario.
 * Devuelve nombreArtistico si es artista, nombre + apellidos si es usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NombreUsuarioDTO {

    /**
     * Nombre a mostrar del usuario.
     * - Para artistas: nombreArtistico
     * - Para usuarios normales: nombre + apellidos
     */
    private String nombreCompleto;

    /**
     * Tipo de usuario (USUARIO o ARTISTA)
     */
    private String tipoUsuario;
}