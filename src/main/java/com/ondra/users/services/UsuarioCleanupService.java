package com.ondra.users.services;

import com.ondra.users.models.dao.*;
import com.ondra.users.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la limpieza y mantenimiento automático de usuarios.
 *
 * <p>Realiza tareas programadas de limpieza:</p>
 * <ul>
 *   <li>Desactiva cuentas sin verificar después de 7 días</li>
 *   <li>Elimina usuarios inactivos después de 30 días</li>
 *   <li>Limpia tokens de verificación expirados</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioCleanupService {

    private final UsuarioRepository usuarioRepository;
    private final ArtistaRepository artistaRepository;
    private final SeguimientoRepository seguimientoRepository;
    private final RedSocialRepository redSocialRepository;
    private final MetodoPagoUsuarioRepository pagoUsuarioRepository;
    private final MetodoCobroArtistaRepository metodoCobroArtistaRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Desactiva cuentas que no han verificado su email en 7 días.
     *
     * <p>Solo afecta a cuentas creadas con email y contraseña. Las cuentas
     * de Google no se desactivan ya que su email está verificado automáticamente.</p>
     */
    @Transactional
    public void desactivarCuentasSinVerificar() {
        log.info("🔍 Iniciando desactivación de cuentas sin verificar");

        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(7);

        List<Usuario> usuariosNoVerificados = usuarioRepository
                .findByActivoTrueAndEmailVerificadoFalseAndFechaRegistroBefore(fechaLimite);

        if (usuariosNoVerificados.isEmpty()) {
            log.info("No se encontraron cuentas sin verificar para desactivar");
            return;
        }

        log.info("📋 Encontradas {} cuentas sin verificar para desactivar",
                usuariosNoVerificados.size());

        for (Usuario usuario : usuariosNoVerificados) {
            try {
                if (usuario.getGoogleUid() == null || usuario.getGoogleUid().isEmpty()) {
                    usuario.setActivo(false);
                    usuario.setTokenVerificacion(null);
                    usuario.setFechaExpiracionToken(null);

                    usuarioRepository.save(usuario);

                    log.info("⚠️ Cuenta desactivada por falta de verificación: Usuario ID {} ({})",
                            usuario.getIdUsuario(), usuario.getEmailUsuario());
                }
            } catch (Exception e) {
                log.error("Error al desactivar usuario ID {}: {}",
                        usuario.getIdUsuario(), e.getMessage());
            }
        }

        log.info("✅ Desactivación de cuentas sin verificar completada");
    }

    /**
     * Elimina usuarios inactivos por más de 30 días y sus datos asociados.
     *
     * <p>Elimina en cascada:</p>
     * <ul>
     *   <li>Refresh tokens</li>
     *   <li>Seguimientos</li>
     *   <li>Métodos de pago</li>
     *   <li>Perfil de artista (si aplica)</li>
     *   <li>Redes sociales del artista</li>
     *   <li>Métodos de cobro del artista</li>
     * </ul>
     */
    @Transactional
    public void eliminarUsuariosInactivos() {
        log.info("🗑️ Iniciando limpieza de usuarios inactivos");

        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(30);

        List<Usuario> usuariosAEliminar = usuarioRepository
                .findByActivoFalseAndFechaRegistroBefore(fechaLimite);

        if (usuariosAEliminar.isEmpty()) {
            log.info("No se encontraron usuarios inactivos para eliminar");
            return;
        }

        log.info("📋 Encontrados {} usuarios inactivos para eliminar",
                usuariosAEliminar.size());

        for (Usuario usuario : usuariosAEliminar) {
            try {
                eliminarDatosUsuario(usuario);
                usuarioRepository.delete(usuario);

                log.info("✅ Usuario ID {} eliminado correctamente ({})",
                        usuario.getIdUsuario(), usuario.getEmailUsuario());

            } catch (Exception e) {
                log.error("Error al eliminar usuario ID {}: {}",
                        usuario.getIdUsuario(), e.getMessage(), e);
            }
        }

        log.info("✅ Limpieza de usuarios inactivos completada. Total eliminados: {}",
                usuariosAEliminar.size());
    }

    /**
     * Limpia tokens de verificación y recuperación expirados.
     *
     * <p>Solo elimina los tokens expirados, sin afectar las cuentas de usuario.</p>
     */
    @Transactional
    public void limpiarTokensExpirados() {
        log.info("🧹 Iniciando limpieza de tokens expirados");

        LocalDateTime ahora = LocalDateTime.now();

        List<Usuario> usuariosConTokenVerificacion = usuarioRepository
                .findByTokenVerificacionIsNotNullAndFechaExpiracionTokenBefore(ahora);

        if (usuariosConTokenVerificacion.isEmpty()) {
            log.info("No se encontraron tokens expirados para limpiar");
            return;
        }

        for (Usuario usuario : usuariosConTokenVerificacion) {
            usuario.setTokenVerificacion(null);
            usuario.setFechaExpiracionToken(null);
        }

        usuarioRepository.saveAll(usuariosConTokenVerificacion);

        log.info("✅ Limpieza de tokens completada. Total limpiados: {}",
                usuariosConTokenVerificacion.size());
    }

    /**
     * Elimina todos los datos asociados a un usuario antes de su eliminación.
     *
     * @param usuario Usuario cuyos datos se eliminarán
     */
    private void eliminarDatosUsuario(Usuario usuario) {
        Long idUsuario = usuario.getIdUsuario();

        List<RefreshToken> refreshTokens = refreshTokenRepository
                .findByUsuario_IdUsuario(idUsuario);
        if (!refreshTokens.isEmpty()) {
            refreshTokenRepository.deleteAll(refreshTokens);
            log.debug("🔑 Eliminados {} refresh tokens para usuario ID {}",
                    refreshTokens.size(), idUsuario);
        }

        long totalSeguimientos = seguimientoRepository.countBySeguidorIdUsuario(idUsuario)
                + seguimientoRepository.countBySeguidoIdUsuario(idUsuario);

        if (totalSeguimientos > 0) {
            seguimientoRepository.deleteBySeguidorIdUsuarioOrSeguidoIdUsuario(
                    idUsuario, idUsuario);
            log.debug("👥 Eliminados {} seguimientos para usuario ID {}",
                    totalSeguimientos, idUsuario);
        }

        List<MetodoPagoUsuario> metodosDeUsuario = pagoUsuarioRepository
                .findByUsuario_IdUsuario(idUsuario);
        if (!metodosDeUsuario.isEmpty()) {
            pagoUsuarioRepository.deleteAll(metodosDeUsuario);
            log.debug("💳 Eliminados {} métodos de pago para usuario ID {}",
                    metodosDeUsuario.size(), idUsuario);
        }

        Optional<Artista> artistaOpt = artistaRepository
                .findByUsuario_IdUsuario(idUsuario);
        if (artistaOpt.isPresent()) {
            eliminarDatosArtista(artistaOpt.get());
        }
    }

    /**
     * Elimina todos los datos asociados a un perfil de artista.
     *
     * @param artista Artista cuyos datos se eliminarán
     */
    private void eliminarDatosArtista(Artista artista) {
        Long idArtista = artista.getIdArtista();

        List<RedSocial> redesSociales = redSocialRepository
                .findByArtista_IdArtista(idArtista);
        if (!redesSociales.isEmpty()) {
            redSocialRepository.deleteAll(redesSociales);
            log.debug("🔗 Eliminadas {} redes sociales para artista ID {}",
                    redesSociales.size(), idArtista);
        }

        List<MetodoCobroArtista> metodosCobroArtista = metodoCobroArtistaRepository
                .findByArtista_IdArtista(idArtista);
        if (!metodosCobroArtista.isEmpty()) {
            metodoCobroArtistaRepository.deleteAll(metodosCobroArtista);
            log.debug("💰 Eliminados {} métodos de cobro para artista ID {}",
                    metodosCobroArtista.size(), idArtista);
        }

        artistaRepository.delete(artista);
        log.debug("🎤 Eliminado perfil de artista ID {}", idArtista);
    }
}