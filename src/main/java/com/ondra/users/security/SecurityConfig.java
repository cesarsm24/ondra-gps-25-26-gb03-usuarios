package com.ondra.users.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.List;

/**
 * Configuración de seguridad de Spring Security.
 */
@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ServiceTokenFilter serviceTokenFilter;

    // Solución Code Smell 1: Constante para rol duplicado
    private static final String ROLE_SERVICE = "SERVICE";

    // Solución Code Smell 2: Constante para path de artistas duplicado
    private static final String ARTIST_PATH_WITH_ID = "/api/artistas/{id}";

    // Solución Code Smell 3: Constante para path de usuarios duplicado
    private static final String USER_PATH_WITH_ID = "/api/usuarios/{id}";

    // Configuración adicional de paths para mayor mantenibilidad
    private static final String API_USUARIOS = "/api/usuarios";
    private static final String API_ARTISTAS = "/api/artistas";
    private static final String API_SEGUIMIENTOS = "/api/seguimientos";
    private static final String API_PUBLIC = "/api/public/**";
    private static final String API_INTERNAL = "/api/internal/**";

    @Value("${cors.allowed-origins:http://localhost:4200}")
    private List<String> allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Configuración pública
                        .requestMatchers(HttpMethod.GET, "/api/config/public").permitAll()
                        .requestMatchers(HttpMethod.GET, API_USUARIOS + "/stats").permitAll()

                        // Registro y autenticación - Públicos
                        .requestMatchers(HttpMethod.POST, API_USUARIOS).permitAll()
                        .requestMatchers(HttpMethod.POST, API_USUARIOS + "/login").permitAll()
                        .requestMatchers(HttpMethod.POST, API_USUARIOS + "/login/google").permitAll()
                        .requestMatchers(HttpMethod.POST, API_USUARIOS + "/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, API_USUARIOS + "/logout").permitAll()

                        // Endpoints internos - Solo para microservicios
                        .requestMatchers(API_INTERNAL).hasRole(ROLE_SERVICE)
                        .requestMatchers(HttpMethod.GET, API_USUARIOS + "/*/datos-usuario").hasRole(ROLE_SERVICE)
                        .requestMatchers(HttpMethod.GET, API_USUARIOS + "/*/existe").hasRole(ROLE_SERVICE)

                        // Recuperación de contraseña - Públicos
                        .requestMatchers(HttpMethod.POST, API_USUARIOS + "/recuperar-password").permitAll()
                        .requestMatchers(HttpMethod.POST, API_USUARIOS + "/restablecer-password").permitAll()

                        // Verificación de email - Públicos
                        .requestMatchers(HttpMethod.GET, API_USUARIOS + "/verificar-email").permitAll()
                        .requestMatchers(HttpMethod.POST, API_USUARIOS + "/reenviar-verificacion").permitAll()

                        // Endpoints públicos generales
                        .requestMatchers(HttpMethod.GET, API_PUBLIC).permitAll()

                        // Artistas - Consultas públicas
                        .requestMatchers(HttpMethod.GET, API_ARTISTAS).permitAll()
                        .requestMatchers(HttpMethod.GET, ARTIST_PATH_WITH_ID).permitAll()
                        .requestMatchers(HttpMethod.GET, ARTIST_PATH_WITH_ID + "/redes").permitAll()

                        // Artistas - Métodos de cobro (requieren autenticación)
                        .requestMatchers(ARTIST_PATH_WITH_ID + "/metodos-cobro/**").authenticated()

                        // Artistas - Redes sociales (requieren autenticación)
                        .requestMatchers(HttpMethod.POST, ARTIST_PATH_WITH_ID + "/redes").authenticated()
                        .requestMatchers(HttpMethod.PUT, ARTIST_PATH_WITH_ID + "/redes/{id_red}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, ARTIST_PATH_WITH_ID + "/redes/{id_red}").authenticated()

                        // Artistas - Gestión (requieren autenticación)
                        .requestMatchers(HttpMethod.POST, API_ARTISTAS).authenticated()
                        .requestMatchers(HttpMethod.PUT, ARTIST_PATH_WITH_ID).authenticated()
                        .requestMatchers(HttpMethod.DELETE, ARTIST_PATH_WITH_ID).authenticated()
                        .requestMatchers(HttpMethod.POST, ARTIST_PATH_WITH_ID + "/renunciar").authenticated()

                        // Usuarios - Métodos de pago (requieren autenticación)
                        .requestMatchers(USER_PATH_WITH_ID + "/metodos-pago/**").authenticated()

                        // Usuarios - Gestión de perfil (requieren autenticación)
                        .requestMatchers(HttpMethod.GET, USER_PATH_WITH_ID).authenticated()
                        .requestMatchers(HttpMethod.PUT, USER_PATH_WITH_ID).authenticated()
                        .requestMatchers(HttpMethod.DELETE, USER_PATH_WITH_ID).authenticated()
                        .requestMatchers(HttpMethod.PATCH, USER_PATH_WITH_ID + "/onboarding-completado").authenticated()
                        .requestMatchers(HttpMethod.PUT, USER_PATH_WITH_ID + "/cambiar-password").authenticated()
                        .requestMatchers(HttpMethod.POST, API_USUARIOS + "/logout-all").authenticated()

                        // Seguimientos - Consultas públicas
                        .requestMatchers(HttpMethod.GET, API_SEGUIMIENTOS + "/{idUsuario}/seguidos").permitAll()
                        .requestMatchers(HttpMethod.GET, API_SEGUIMIENTOS + "/{idUsuario}/seguidores").permitAll()
                        .requestMatchers(HttpMethod.GET, API_SEGUIMIENTOS + "/{idUsuario}/estadisticas").permitAll()

                        // Seguimientos - Acciones (requieren autenticación)
                        .requestMatchers(HttpMethod.POST, API_SEGUIMIENTOS).authenticated()
                        .requestMatchers(HttpMethod.DELETE, API_SEGUIMIENTOS + "/{idUsuario}").authenticated()
                        .requestMatchers(HttpMethod.GET, API_SEGUIMIENTOS + "/{idUsuario}/verificar").authenticated()

                        // Actuator
                        .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()

                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated()
                )

                // Orden de filtros: ServiceToken -> JWT -> UsernamePassword
                .addFilterBefore(serviceTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("🔒 SecurityFilterChain configurado correctamente");
        log.info("📋 Filtros registrados: ServiceTokenFilter -> JwtAuthenticationFilter");
        return http.build();
    }
}