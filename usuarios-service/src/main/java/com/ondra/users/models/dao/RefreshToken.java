package com.ondra.users.models.dao;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa un token de refresco para mantener sesiones de usuario.
 *
 * <p>Los refresh tokens permiten obtener nuevos access tokens sin requerir
 * credenciales nuevamente. Se almacenan en la base de datos para poder
 * revocarlos cuando sea necesario (logout, cambio de contraseña, etc.)</p>
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_refresh_token")
    private Long idRefreshToken;

    /**
     * Token único generado para este refresh token.
     * Se envía al cliente como HttpOnly Cookie.
     */
    @Column(name = "token", nullable = false, unique = true, length = 500)
    private String token;

    /**
     * Usuario al que pertenece este refresh token.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    /**
     * Fecha y hora de expiración del token.
     */
    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    /**
     * Indica si el token ha sido revocado manualmente.
     * Los tokens revocados no pueden usarse para obtener access tokens.
     */
    @Column(name = "revocado", nullable = false)
    private boolean revocado;

    /**
     * Fecha y hora de creación del token.
     */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Verifica si el refresh token ha expirado.
     *
     * @return true si el token ha expirado, false en caso contrario
     */
    public boolean haExpirado() {
        return LocalDateTime.now().isAfter(this.fechaExpiracion);
    }

    /**
     * Verifica si el refresh token es válido para usar.
     * Un token es válido si no ha expirado y no ha sido revocado.
     *
     * @return true si el token es válido, false en caso contrario
     */
    public boolean esValido() {
        return !haExpirado() && !revocado;
    }
}