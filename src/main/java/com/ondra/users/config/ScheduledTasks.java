package com.ondra.users.config;

import com.ondra.users.security.JwtService;
import com.ondra.users.services.UsuarioCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Tareas programadas para el mantenimiento del sistema.
 *
 * <p>Centraliza todas las tareas automáticas:</p>
 * <ul>
 *   <li>Limpieza de refresh tokens expirados (diario - 3:00 AM)</li>
 *   <li>Desactivación de cuentas sin verificar después de 7 días (semanal - lunes 2:00 AM)</li>
 *   <li>Eliminación de usuarios inactivos después de 30 días (semanal - lunes 3:00 AM)</li>
 *   <li>Limpieza de tokens de verificación y recuperación expirados (diario - 4:00 AM)</li>
 * </ul>
 *
 * <p><strong>Programación de tareas:</strong></p>
 * <table>
 *   <tr><th>Tarea</th><th>Frecuencia</th><th>Horario</th></tr>
 *   <tr><td>Refresh Tokens</td><td>Diario</td><td>3:00 AM</td></tr>
 *   <tr><td>Cuentas sin verificar</td><td>Semanal (Lunes)</td><td>2:00 AM</td></tr>
 *   <tr><td>Usuarios inactivos</td><td>Semanal (Lunes)</td><td>3:00 AM</td></tr>
 *   <tr><td>Tokens expirados</td><td>Diario</td><td>4:00 AM</td></tr>
 * </table>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final JwtService jwtService;
    private final UsuarioCleanupService usuarioCleanupService;

    /**
     * Limpia refresh tokens expirados de la base de datos.
     *
     * <p><strong>Programación:</strong> Diario a las 3:00 AM</p>
     * <p><strong>Criterio:</strong> Tokens cuya fecha de expiración ya pasó</p>
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void limpiarRefreshTokensExpirados() {
        log.info("⏰ Iniciando limpieza de refresh tokens expirados");
        try {
            jwtService.limpiarTokensExpirados();
            log.info("✅ Limpieza de refresh tokens completada");
        } catch (Exception e) {
            log.error("❌ Error al limpiar refresh tokens: {}", e.getMessage(), e);
        }
    }

    /**
     * Desactiva cuentas que no han verificado su email en 7 días.
     *
     * <p><strong>Programación:</strong> Cada lunes a las 2:00 AM</p>
     * <p><strong>Criterio:</strong> Cuentas activas, sin verificar email, creadas hace más de 7 días</p>
     * <p><strong>Acción:</strong> Desactiva la cuenta y limpia tokens de verificación</p>
     *
     * <p><b>Nota:</b> No afecta a cuentas creadas con Google OAuth (se verifican automáticamente)</p>
     */
    @Scheduled(cron = "0 0 2 * * MON")
    public void desactivarCuentasSinVerificar() {
        log.info("⏰ Iniciando desactivación de cuentas sin verificar (>7 días)");
        try {
            usuarioCleanupService.desactivarCuentasSinVerificar();
            log.info("✅ Desactivación de cuentas sin verificar completada");
        } catch (Exception e) {
            log.error("❌ Error al desactivar cuentas sin verificar: {}", e.getMessage(), e);
        }
    }

    /**
     * Elimina usuarios inactivos por más de 30 días.
     *
     * <p><strong>Programación:</strong> Cada lunes a las 3:00 AM</p>
     * <p><strong>Criterio:</strong> Cuentas inactivas (activo=false) creadas hace más de 30 días</p>
     * <p><strong>Acción:</strong> Elimina permanentemente el usuario y todos sus datos asociados:</p>
     * <ul>
     *   <li>Refresh tokens</li>
     *   <li>Seguimientos (como seguidor y seguido)</li>
     *   <li>Métodos de pago</li>
     *   <li>Perfil de artista (si aplica)</li>
     *   <li>Redes sociales del artista</li>
     *   <li>Métodos de cobro del artista</li>
     * </ul>
     *
     * <p><b>⚠️ Advertencia:</b> Esta operación es irreversible y elimina todos los datos del usuario</p>
     */
    @Scheduled(cron = "0 0 3 * * MON")
    public void eliminarUsuariosInactivos() {
        log.info("⏰ Iniciando limpieza de usuarios inactivos (>30 días)");
        try {
            usuarioCleanupService.eliminarUsuariosInactivos();
            log.info("✅ Limpieza de usuarios inactivos completada");
        } catch (Exception e) {
            log.error("❌ Error al eliminar usuarios inactivos: {}", e.getMessage(), e);
        }
    }

    /**
     * Limpia tokens de verificación y recuperación expirados.
     *
     * <p><strong>Programación:</strong> Diario a las 4:00 AM</p>
     * <p><strong>Criterio:</strong> Tokens cuya fecha de expiración ya pasó</p>
     * <p><strong>Acción:</strong> Limpia los siguientes campos de la tabla usuarios:</p>
     * <ul>
     *   <li>tokenVerificacion</li>
     *   <li>fechaExpiracionToken</li>
     *   <li>tokenRecuperacion</li>
     *   <li>fechaExpiracionTokenRecuperacion</li>
     *   <li>codigoRecuperacion</li>
     * </ul>
     *
     * <p><b>Nota:</b> Solo limpia tokens, no afecta las cuentas de usuario</p>
     */
    @Scheduled(cron = "0 0 4 * * *")
    public void limpiarTokensVerificacionExpirados() {
        log.info("⏰ Iniciando limpieza de tokens de verificación y recuperación expirados");
        try {
            usuarioCleanupService.limpiarTokensExpirados();
            log.info("✅ Limpieza de tokens de verificación y recuperación completada");
        } catch (Exception e) {
            log.error("❌ Error al limpiar tokens: {}", e.getMessage(), e);
        }
    }
}