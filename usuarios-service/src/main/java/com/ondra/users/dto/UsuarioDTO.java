package com.ondra.users.dto;

import com.ondra.users.models.enums.TipoUsuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa la información de un usuario en el sistema.
 *
 * <p>Incluye datos personales, tipo de usuario, estado de cuenta y preferencias de autenticación.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioDTO {

    /**
     * Identificador único del usuario.
     */
    private Long idUsuario;

    /**
     * Correo electrónico del usuario.
     */
    private String emailUsuario;

    /**
     * Nombre del usuario.
     */
    private String nombreUsuario;

    /**
     * Apellidos del usuario.
     */
    private String apellidosUsuario;

    /**
     * Tipo de usuario.
     *
     * <p>Ejemplos: normal, artista, administrador, etc.</p>
     */
    private TipoUsuario tipoUsuario;

    /**
     * URL de la foto de perfil del usuario.
     */
    private String fotoPerfil;

    /**
     * Indica si la cuenta del usuario está activa.
     */
    private boolean activo;

    /**
     * Indica si el usuario puede autenticarse mediante Google/Firebase.
     */
    private boolean permiteGoogle;

    private boolean emailVerificado;
}