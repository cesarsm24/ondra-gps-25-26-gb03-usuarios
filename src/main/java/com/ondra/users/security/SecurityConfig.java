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

    // Endpoints específicos configurables
    @Value("${security.endpoints.config.public:/public}")
    private String endpointConfigPublic;

    @Value("${security.endpoints.usuarios.stats:/stats}")
    private String endpointUsuariosStats;

    @Value("${security.endpoints.usuarios.login:/login}")
    private String endpointUsuariosLogin;

    @Value("${security.endpoints.usuarios.login-google:/login/google}")
    private String endpointUsuariosLoginGoogle;

    @Value("${security.endpoints.usuarios.refresh:/refresh}")
    private String endpointUsuariosRefresh;

    @Value("${security.endpoints.usuarios.logout:/logout}")
    private String endpointUsuariosLogout;

    @Value("${security.endpoints.usuarios.logout-all:/logout-all}")
    private String endpointUsuariosLogoutAll;

    @Value("${security.endpoints.usuarios.recuperar-password:/recuperar-password}")
    private String endpointRecuperarPassword;

    @Value("${security.endpoints.usuarios.restablecer-password:/restablecer-password}")
    private String endpointRestablecerPassword;

    @Value("${security.endpoints.usuarios.verificar-email:/verificar-email}")
    private String endpointVerificarEmail;

    @Value("${security.endpoints.usuarios.reenviar-verificacion:/reenviar-verificacion}")
    private String endpointReenviarVerificacion;

    @Value("${security.endpoints.usuarios.datos-usuario:/*/datos-usuario}")
    private String endpointDatosUsuario;

    @Value("${security.endpoints.usuarios.existe:/*/existe}")
    private String endpointUsuarioExiste;

    @Value("${security.endpoints.usuarios.onboarding:/onboarding-completado}")
    private String endpointOnboarding;

    @Value("${security.endpoints.usuarios.cambiar-password:/cambiar-password}")
    private String endpointCambiarPassword;

    @Value("${security.endpoints.artistas.redes:/redes}")
    private String endpointArtistasRedes;

    @Value("${security.endpoints.artistas.metodos-cobro:/metodos-cobro}")
    private String endpointMetodosCobro;

    @Value("${security.endpoints.artistas.renunciar:/renunciar}")
    private String endpointRenunciar;

    @Value("${security.endpoints.usuarios.metodos-pago:/metodos-pago}")
    private String endpointMetodosPago;

    @Value("${security.endpoints.seguimientos.seguidos:/seguidos}")
    private String endpointSeguidos;

    @Value("${security.endpoints.seguimientos.seguidores:/seguidores}")
    private String endpointSeguidores;

    @Value("${security.endpoints.seguimientos.estadisticas:/estadisticas}")
    private String endpointEstadisticas;

    @Value("${security.endpoints.seguimientos.verificar:/verificar}")
    private String endpointVerificar;

    @Value("${security.endpoints.actuator.health:/health}")
    private String endpointActuatorHealth;

    // Path patterns - Constantes para patrones reutilizables
    private static final String PATH_ID = "/{id}";
    private static final String PATH_ID_USUARIO = "/{idUsuario}";
    private static final String PATH_ID_RED = "/{id_red}";
    private static final String PATH_WILDCARD = "/**";

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
        source.registerCorsConfiguration(PATH_WILDCARD, configuration);

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
                        .requestMatchers(HttpMethod.GET, apiConfig + endpointConfigPublic).permitAll()
                        .requestMatchers(HttpMethod.GET, apiUsuarios + endpointUsuariosStats).permitAll()

                        // Registro y autenticación - Públicos
                        .requestMatchers(HttpMethod.POST, apiUsuarios).permitAll()
                        .requestMatchers(HttpMethod.POST, apiUsuarios + endpointUsuariosLogin).permitAll()
                        .requestMatchers(HttpMethod.POST, apiUsuarios + endpointUsuariosLoginGoogle).permitAll()
                        .requestMatchers(HttpMethod.POST, apiUsuarios + endpointUsuariosRefresh).permitAll()
                        .requestMatchers(HttpMethod.POST, apiUsuarios + endpointUsuariosLogout).permitAll()

                        // Endpoints internos - Solo para microservicios
                        .requestMatchers(apiInternal + PATH_WILDCARD).hasRole(roleService)
                        .requestMatchers(HttpMethod.GET, apiUsuarios + endpointDatosUsuario).hasRole(roleService)
                        .requestMatchers(HttpMethod.GET, apiUsuarios + endpointUsuarioExiste).hasRole(roleService)

                        // Recuperación de contraseña - Públicos
                        .requestMatchers(HttpMethod.POST, apiUsuarios + endpointRecuperarPassword).permitAll()
                        .requestMatchers(HttpMethod.POST, apiUsuarios + endpointRestablecerPassword).permitAll()

                        // Verificación de email - Públicos
                        .requestMatchers(HttpMethod.GET, apiUsuarios + endpointVerificarEmail).permitAll()
                        .requestMatchers(HttpMethod.POST, apiUsuarios + endpointReenviarVerificacion).permitAll()

                        // Endpoints públicos generales
                        .requestMatchers(HttpMethod.GET, apiPublic + PATH_WILDCARD).permitAll()

                        // Artistas - Consultas públicas
                        .requestMatchers(HttpMethod.GET, apiArtistas).permitAll()
                        .requestMatchers(HttpMethod.GET, apiArtistas + PATH_ID).permitAll()
                        .requestMatchers(HttpMethod.GET, apiArtistas + PATH_ID + endpointArtistasRedes).permitAll()

                        // Artistas - Métodos de cobro (requieren autenticación)
                        .requestMatchers(apiArtistas + PATH_ID + endpointMetodosCobro + PATH_WILDCARD).authenticated()

                        // Artistas - Redes sociales (requieren autenticación)
                        .requestMatchers(HttpMethod.POST, apiArtistas + PATH_ID + endpointArtistasRedes).authenticated()
                        .requestMatchers(HttpMethod.PUT, apiArtistas + PATH_ID + endpointArtistasRedes + PATH_ID_RED).authenticated()
                        .requestMatchers(HttpMethod.DELETE, apiArtistas + PATH_ID + endpointArtistasRedes + PATH_ID_RED).authenticated()

                        // Artistas - Gestión (requieren autenticación)
                        .requestMatchers(HttpMethod.POST, apiArtistas).authenticated()
                        .requestMatchers(HttpMethod.PUT, apiArtistas + PATH_ID).authenticated()
                        .requestMatchers(HttpMethod.DELETE, apiArtistas + PATH_ID).authenticated()
                        .requestMatchers(HttpMethod.POST, apiArtistas + PATH_ID + endpointRenunciar).authenticated()

                        // Usuarios - Métodos de pago (requieren autenticación)
                        .requestMatchers(apiUsuarios + PATH_ID + endpointMetodosPago + PATH_WILDCARD).authenticated()

                        // Usuarios - Gestión de perfil (requieren autenticación)
                        .requestMatchers(HttpMethod.GET, apiUsuarios + PATH_ID).authenticated()
                        .requestMatchers(HttpMethod.PUT, apiUsuarios + PATH_ID).authenticated()
                        .requestMatchers(HttpMethod.DELETE, apiUsuarios + PATH_ID).authenticated()
                        .requestMatchers(HttpMethod.PATCH, apiUsuarios + PATH_ID + endpointOnboarding).authenticated()
                        .requestMatchers(HttpMethod.PUT, apiUsuarios + PATH_ID + endpointCambiarPassword).authenticated()
                        .requestMatchers(HttpMethod.POST, apiUsuarios + endpointUsuariosLogoutAll).authenticated()

                        // Seguimientos - Consultas públicas
                        .requestMatchers(HttpMethod.GET, apiSeguimientos + PATH_ID_USUARIO + endpointSeguidos).permitAll()
                        .requestMatchers(HttpMethod.GET, apiSeguimientos + PATH_ID_USUARIO + endpointSeguidores).permitAll()
                        .requestMatchers(HttpMethod.GET, apiSeguimientos + PATH_ID_USUARIO + endpointEstadisticas).permitAll()

                        // Seguimientos - Acciones (requieren autenticación)
                        .requestMatchers(HttpMethod.POST, apiSeguimientos).authenticated()
                        .requestMatchers(HttpMethod.DELETE, apiSeguimientos + PATH_ID_USUARIO).authenticated()
                        .requestMatchers(HttpMethod.GET, apiSeguimientos + PATH_ID_USUARIO + endpointVerificar).authenticated()

                        // Actuator
                        .requestMatchers(HttpMethod.GET, apiActuator + endpointActuatorHealth).permitAll()

                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated()
                )

                // Orden de filtros: ServiceToken -> JWT -> UsernamePassword
                .addFilterBefore(serviceTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        logSecurityConfiguration();

        return http.build();
    }

    /**
     * Registra la configuración de seguridad en los logs.
     */
    private void logSecurityConfiguration() {
        log.info("🔒 SecurityFilterChain configurado correctamente");
        log.info("📋 Filtros registrados: ServiceTokenFilter -> JwtAuthenticationFilter");
        log.info("🌐 Rutas base configuradas:");
        log.info("   - Usuarios: {}", apiUsuarios);
        log.info("   - Artistas: {}", apiArtistas);
        log.info("   - Seguimientos: {}", apiSeguimientos);
        log.info("   - Public: {}", apiPublic);
        log.info("   - Internal: {}", apiInternal);
        log.info("   - Actuator: {}", apiActuator);
    }
}