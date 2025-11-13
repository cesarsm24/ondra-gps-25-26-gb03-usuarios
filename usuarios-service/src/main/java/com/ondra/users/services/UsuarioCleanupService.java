package com.ondra.users.services;

import com.ondra.users.models.dao.*;
import com.ondra.users.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio encargado de la limpieza y mantenimiento de usuarios.
 *
 * <p>Realiza dos tareas automáticas:</p>
 * <ul>
 *   <li>Desactiva cuentas sin verificar después de 7 días</li>
 *   <li>Elimina usuarios inactivos después de 30 días</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioCleanupService {

    private final UsuarioRepository usuarioRepository;
    private final SeguimientoRepository seguimientoRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Desactiva cuentas que no han verificado su email en 7 días.
     *
     * <p>Se ejecuta automáticamente cada lunes a las 2:00 AM.</p>
     * <p>Solo afecta a cuentas creadas con email/contraseña (no Google).</p>
     */
    @Transactional
    public void desactivarCuentasSinVerificar() {
        log.info("Iniciando desactivación de cuentas sin verificar...");

        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(7);

        // Buscar usuarios activos, sin verificar y con más de 7 días de antigüedad
        List<Usuario> usuariosNoVerificados = usuarioRepository
                .findByActivoTrueAndEmailVerificadoFalseAndFechaRegistroBefore(fechaLimite);

        if (!usuariosNoVerificados.isEmpty()) {
            log.info("Encontradas {} cuentas sin verificar para desactivar", usuariosNoVerificados.size());

            for (Usuario usuario : usuariosNoVerificados) {
                try {
                    // Solo desactivar si NO es cuenta de Google (Google verifica automáticamente)
                    if (usuario.getGoogleUid() == null || usuario.getGoogleUid().isEmpty()) {
                        usuario.setActivo(false);

                        // Limpiar tokens de verificación expirados
                        usuario.setTokenVerificacion(null);
                        usuario.setFechaExpiracionToken(null);

                        usuarioRepository.save(usuario);

                        log.info("Cuenta desactivada por falta de verificación: Usuario ID {} (Email: {})",
                                usuario.getIdUsuario(), usuario.getEmailUsuario());
                    }
                } catch (Exception e) {
                    log.error("Error al desactivar usuario ID {}: {}", usuario.getIdUsuario(), e.getMessage());
                }
            }

            log.info("Desactivación de cuentas sin verificar completada");
        } else {
            log.info("No se encontraron cuentas sin verificar para desactivar");
        }
    }

    /**
     * Elimina usuarios inactivos por más de 30 días.
     *
     * <p>Se ejecuta automáticamente el primer día de cada mes a las 3:00 AM.
     * También elimina métodos de pago, artistas, redes sociales, métodos de cobro
     * y seguimientos asociados.</p>
     */
    @Transactional
    public void eliminarUsuariosInactivos() {
        log.info("Iniciando limpieza de usuarios inactivos...");

        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(30);

        List<Usuario> usuariosAEliminar = usuarioRepository
                .findByActivoFalseAndFechaRegistroBefore(fechaLimite);

        if (!usuariosAEliminar.isEmpty()) {
            log.info("Encontrados {} usuarios inactivos para eliminar", usuariosAEliminar.size());

            for (Usuario usuario : usuariosAEliminar) {
                try {
                    // 1. Eliminar refresh tokens
                    List<RefreshToken> refreshTokens = refreshTokenRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
                    if (!refreshTokens.isEmpty()) {
                        refreshTokenRepository.deleteAll(refreshTokens);
                        log.debug("Eliminados {} refresh tokens para usuario ID {}", refreshTokens.size(), usuario.getIdUsuario());
                    }

                    // 2. Eliminar todos los seguimientos relacionados (como seguidor y como seguido)
                    long totalSeguimientos = seguimientoRepository.countBySeguidorIdUsuario(usuario.getIdUsuario())
                            + seguimientoRepository.countBySeguidoIdUsuario(usuario.getIdUsuario());

                    if (totalSeguimientos > 0) {
                        seguimientoRepository.deleteBySeguidorIdUsuarioOrSeguidoIdUsuario(
                                usuario.getIdUsuario(),
                                usuario.getIdUsuario()
                        );
                        log.debug("Eliminados {} seguimientos totales para usuario ID {}",
                                totalSeguimientos, usuario.getIdUsuario());
                    }

                    // 5. Eliminar usuario
                    usuarioRepository.delete(usuario);

                    log.info("Usuario ID {} eliminado correctamente (Email: {})",
                            usuario.getIdUsuario(), usuario.getEmailUsuario());

                } catch (Exception e) {
                    log.error("Error al eliminar usuario ID {}: {}", usuario.getIdUsuario(), e.getMessage(), e);
                }
            }

            log.info("Limpieza de usuarios inactivos completada. Total eliminados: {}", usuariosAEliminar.size());
        } else {
            log.info("No se encontraron usuarios inactivos para eliminar");
        }
    }

    /**
     * Limpia tokens de verificación y recuperación expirados.
     *
     * <p>Se ejecuta cada día a las 4:00 AM.</p>
     * <p>Solo limpia los tokens, no afecta a las cuentas.</p>
     */
    @Transactional
    public void limpiarTokensExpirados() {
        log.info("Iniciando limpieza de tokens expirados...");

        LocalDateTime ahora = LocalDateTime.now();
        int tokensLimpiados = 0;

        // Limpiar tokens de verificación de email expirados
        List<Usuario> usuariosConTokenVerificacion = usuarioRepository
                .findByTokenVerificacionIsNotNullAndFechaExpiracionTokenBefore(ahora);

        for (Usuario usuario : usuariosConTokenVerificacion) {
            usuario.setTokenVerificacion(null);
            usuario.setFechaExpiracionToken(null);
            tokensLimpiados++;
        }

        if (tokensLimpiados > 0) {
            usuarioRepository.saveAll(usuariosConTokenVerificacion);
            log.info("Limpieza de tokens completada. Total limpiados: {}", tokensLimpiados);
        } else {
            log.info("No se encontraron tokens expirados para limpiar");
        }
    }
}