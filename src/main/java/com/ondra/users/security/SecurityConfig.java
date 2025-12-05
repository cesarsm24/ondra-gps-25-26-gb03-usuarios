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

    // Roles
    @Value("${security.role.service:SERVICE}")
    private String roleService;

    // Base paths - Configurables desde properties
    @Value("${api.base.usuarios:/api/usuarios}")
    private String apiUsuarios;

    @Value("${api.base.artistas:/api/artistas}")
    private String apiArtistas;

    @Value("${api.base.seguimientos:/api/seguimientos}")
    private String apiSeguimientos;

    @Value("${api.base.config:/api/config}")
    private String apiConfig;

    @Value("${api.base.public:/api/public}")
    private String apiPublic;

    @Value("${api.base.internal:/api/internal}")
    private String apiInternal;

    @Value("${api.base.actuator:/actuator}")
    private String apiActuator;

    // CORS Configuration
    @Value("${cors.allowed-origins:http://localhost:4200}")
    private List<String> allowedOrigins;

    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS,PATCH}")
    private List<String> allowedMethods;

    @Value("${cors.max-age:3600}")
    private Long corsMaxAge;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(allowedMethods);
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(corsMaxAge);
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
                        .requestMatchers(HttpMethod.GET, apiConfig + "/public").permitAll()
                        .requestMatchers(HttpMethod.GET, apiUsuarios + "/stats").permitAll()

                        // Registro y autenticación - Públicos
                        .requestMatchers(HttpMethod.POST, apiUsuarios).permitAll()
                        .requestMatchers(HttpMethod.POST, apiUsuarios + "/login").permitAll()
                        .requestMatchers(HttpMethod.POST, apiUsuarios + "/login/google").permitAll()
                        .requestMatchers(HttpMethod.POST, apiUsuarios + "/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, apiUsuarios + "/logout").permitAll()

                        // Endpoints internos - Solo para microservicios
                        .requestMatchers(apiInternal + "/**").hasRole(roleService)
                        .requestMatchers(HttpMethod.GET, apiUsuarios + "/*/datos-usuario").hasRole(roleService)
                        .requestMatchers(HttpMethod.GET, apiUsuarios + "/*/existe").hasRole(roleService)

                        // Recuperación de contraseña - Públicos
                        .requestMatchers(HttpMethod.POST, apiUsuarios + "/recuperar-password").permitAll()
                        .requestMatchers(HttpMethod.POST, apiUsuarios + "/restablecer-password").permitAll()

                        // Verificación de email - Públicos
                        .requestMatchers(HttpMethod.GET, apiUsuarios + "/verificar-email").permitAll()
                        .requestMatchers(HttpMethod.POST, apiUsuarios + "/reenviar-verificacion").permitAll()

                        // Endpoints públicos generales
                        .requestMatchers(HttpMethod.GET, apiPublic + "/**").permitAll()

                        // Artistas - Consultas públicas
                        .requestMatchers(HttpMethod.GET, apiArtistas).permitAll()
                        .requestMatchers(HttpMethod.GET, apiArtistas + "/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, apiArtistas + "/{id}/redes").permitAll()

                        // Artistas - Métodos de cobro (requieren autenticación)
                        .requestMatchers(apiArtistas + "/{id}/metodos-cobro/**").authenticated()

                        // Artistas - Redes sociales (requieren autenticación)
                        .requestMatchers(HttpMethod.POST, apiArtistas + "/{id}/redes").authenticated()
                        .requestMatchers(HttpMethod.PUT, apiArtistas + "/{id}/redes/{id_red}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, apiArtistas + "/{id}/redes/{id_red}").authenticated()

                        // Artistas - Gestión (requieren autenticación)
                        .requestMatchers(HttpMethod.POST, apiArtistas).authenticated()
                        .requestMatchers(HttpMethod.PUT, apiArtistas + "/{id}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, apiArtistas + "/{id}").authenticated()
                        .requestMatchers(HttpMethod.POST, apiArtistas + "/{id}/renunciar").authenticated()

                        // Usuarios - Métodos de pago (requieren autenticación)
                        .requestMatchers(apiUsuarios + "/{id}/metodos-pago/**").authenticated()

                        // Usuarios - Gestión de perfil (requieren autenticación)
                        .requestMatchers(HttpMethod.GET, apiUsuarios + "/{id}").authenticated()
                        .requestMatchers(HttpMethod.PUT, apiUsuarios + "/{id}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, apiUsuarios + "/{id}").authenticated()
                        .requestMatchers(HttpMethod.PATCH, apiUsuarios + "/{id}/onboarding-completado").authenticated()
                        .requestMatchers(HttpMethod.PUT, apiUsuarios + "/{id}/cambiar-password").authenticated()
                        .requestMatchers(HttpMethod.POST, apiUsuarios + "/logout-all").authenticated()

                        // Seguimientos - Consultas públicas
                        .requestMatchers(HttpMethod.GET, apiSeguimientos + "/{idUsuario}/seguidos").permitAll()
                        .requestMatchers(HttpMethod.GET, apiSeguimientos + "/{idUsuario}/seguidores").permitAll()
                        .requestMatchers(HttpMethod.GET, apiSeguimientos + "/{idUsuario}/estadisticas").permitAll()

                        // Seguimientos - Acciones (requieren autenticación)
                        .requestMatchers(HttpMethod.POST, apiSeguimientos).authenticated()
                        .requestMatchers(HttpMethod.DELETE, apiSeguimientos + "/{idUsuario}").authenticated()
                        .requestMatchers(HttpMethod.GET, apiSeguimientos + "/{idUsuario}/verificar").authenticated()

                        // Actuator
                        .requestMatchers(HttpMethod.GET, apiActuator + "/health").permitAll()

                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated()
                )

                // Orden de filtros: ServiceToken -> JWT -> UsernamePassword
                .addFilterBefore(serviceTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("🔒 SecurityFilterChain configurado correctamente");
        log.info("📋 Filtros registrados: ServiceTokenFilter -> JwtAuthenticationFilter");
        log.info("🌐 Rutas base configuradas:");
        log.info("   - Usuarios: {}", apiUsuarios);
        log.info("   - Artistas: {}", apiArtistas);
        log.info("   - Seguimientos: {}", apiSeguimientos);
        log.info("   - Public: {}", apiPublic);
        log.info("   - Internal: {}", apiInternal);

        return http.build();
    }
}