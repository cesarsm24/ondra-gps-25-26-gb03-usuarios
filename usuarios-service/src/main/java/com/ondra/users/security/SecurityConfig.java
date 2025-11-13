package com.ondra.users.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuración de seguridad de Spring Security para el microservicio de usuarios.
 *
 *
 * <p><strong>Endpoints públicos (no requieren autenticación):</strong></p>
 * <ul>
 *   <li>Registro y autenticación: /api/usuarios (POST), /api/usuarios/login (POST), etc.</li>
 *   <li>Verificación de email: /api/usuarios/verificar-email (GET)</li>
 *   <li>Recuperación de contraseña: /api/usuarios/recuperar-password (POST), etc.</li>
 *   <li>Perfiles de artistas: /api/artistas (GET), /api/artistas/{id} (GET)</li>
 *   <li>Seguimientos públicos: /api/seguimientos/{idUsuario}/seguidos (GET), etc.</li>
 * </ul>
 *
 * <p><strong>Endpoints protegidos (requieren JWT válido):</strong></p>
 * <ul>
 *   <li>Gestión de perfil de usuario: /api/usuarios/{id} (GET, PUT, DELETE)</li>
 *   <li>Cambio de contraseña: /api/usuarios/{id}/cambiar-password (PUT)</li>
 *   <li>Logout global: /api/usuarios/logout-all (POST)</li>
 *   <li>Gestión de artistas: /api/artistas/{id} (PUT, DELETE)</li>
 *   <li>Seguimientos: /api/seguimientos (POST), /api/seguimientos/{idUsuario} (DELETE)</li>
 *   <li>Imágenes: /api/imagenes/usuario (POST), /api/imagenes/artista (POST)</li>
 *   <li>Métodos de pago: /api/usuarios/{id}/pagos, /api/artistas/{id}/pagos</li>
 *   <li>Redes sociales: /api/artistas/{id}/redes (POST, PUT, DELETE)</li>
 * </ul>
 */
@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Proporciona un codificador de contraseñas utilizando el algoritmo BCrypt.
     *
     * @return instancia de {@link BCryptPasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("✅ Inicializando PasswordEncoder con BCrypt");
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura CORS (Cross-Origin Resource Sharing) para permitir peticiones desde el frontend.
     *
     * @return instancia de {@link CorsConfigurationSource} con la configuración CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("🌐 Configurando CORS para permitir peticiones desde el frontend");

        CorsConfiguration configuration = new CorsConfiguration();

        // Orígenes permitidos (frontend Angular en desarrollo)
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Permitir credenciales (cookies, authorization headers, etc.)
        configuration.setAllowCredentials(true);

        // Tiempo de caché de la respuesta preflight (1 hora)
        configuration.setMaxAge(3600L);

        // Headers expuestos al cliente
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        log.info("✅ CORS configurado: origen permitido = http://localhost:4200");
        return source;
    }

    /**
     * Define la cadena de filtros de seguridad para el servicio.
     *
     * <p>Configura la validación JWT local, desactiva CSRF (API REST stateless),
     * y define qué endpoints son públicos y cuáles requieren autenticación.</p>
     *
     * @param http objeto {@link HttpSecurity} para configurar la seguridad HTTP
     * @return instancia de {@link SecurityFilterChain} con la configuración aplicada
     * @throws Exception si ocurre un error al construir la cadena de seguridad
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("🔧 Configurando SecurityFilterChain con validación JWT local");

        http
                // Habilitar CORS con la configuración definida arriba
                .cors(cors -> {
                    cors.configurationSource(corsConfigurationSource());
                    log.info("🌐 CORS habilitado en Spring Security");
                })

                // Deshabilitar CSRF (APIs REST stateless no lo necesitan)
                .csrf(csrf -> {
                    csrf.disable();
                    log.info("⚠️ Protección CSRF desactivada (API REST stateless)");
                })

                // Configurar política de sesiones (stateless)
                .sessionManagement(session -> {
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                    log.info("📝 Política de sesiones configurada como STATELESS");
                })

                // Configurar autorización de endpoints
                .authorizeHttpRequests(auth -> {
                    log.info("🔐 Configurando autorización de endpoints...");

                    // ========== ENDPOINTS PÚBLICOS - AUTENTICACIÓN ==========
                    auth.requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll(); // Registro
                    auth.requestMatchers(HttpMethod.GET, "/api/usuarios/verificar-email").permitAll(); // Verificar email
                    auth.requestMatchers(HttpMethod.POST, "/api/usuarios/reenviar-verificacion").permitAll(); // Reenviar verificación
                    auth.requestMatchers(HttpMethod.POST, "/api/usuarios/login").permitAll(); // Login tradicional
                    auth.requestMatchers(HttpMethod.POST, "/api/usuarios/login/google").permitAll(); // Login Google
                    auth.requestMatchers(HttpMethod.POST, "/api/usuarios/refresh").permitAll(); // Renovar token
                    auth.requestMatchers(HttpMethod.POST, "/api/usuarios/logout").permitAll(); // Cerrar sesión
                    auth.requestMatchers(HttpMethod.POST, "/api/usuarios/recuperar-password").permitAll(); // Recuperar password
                    auth.requestMatchers(HttpMethod.POST, "/api/usuarios/restablecer-password").permitAll(); // Restablecer password

                    // ========== ENDPOINTS PÚBLICOS - SEGUIMIENTOS ==========
                    auth.requestMatchers(HttpMethod.GET, "/api/seguimientos/{idUsuario}/seguidos").permitAll(); // Ver seguidos
                    auth.requestMatchers(HttpMethod.GET, "/api/seguimientos/{idUsuario}/seguidores").permitAll(); // Ver seguidores
                    auth.requestMatchers(HttpMethod.GET, "/api/seguimientos/{idUsuario}/estadisticas").permitAll(); // Estadísticas

                    // ========== ENDPOINTS PÚBLICOS - ACTUATOR ==========
                    auth.requestMatchers(HttpMethod.GET, "/actuator/health").permitAll(); // Health endpoint público

                    // ========== TODOS LOS DEMÁS ENDPOINTS REQUIEREN AUTENTICACIÓN ==========
                    auth.anyRequest().authenticated();

                    log.info("✅ Endpoints públicos: registro, login, perfiles de artistas, seguimientos públicos, actuator/health");
                    log.info("🔒 Endpoints protegidos: gestión de usuarios, artistas, pagos, imágenes, etc.");
                })

                // Añadir filtro JWT antes del filtro de autenticación estándar
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("✅ SecurityFilterChain inicializada correctamente con validación JWT local y CORS");
        return http.build();
    }
}