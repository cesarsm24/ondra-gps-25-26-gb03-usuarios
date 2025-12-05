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

    // Path parameters - Configurables para mayor flexibilidad
    @Value("${security.path.param.id:/{id}}")
    private String pathId;

    @Value("${security.path.param.id-usuario:/{idUsuario}}")
    private String pathIdUsuario;

    @Value("${security.path.param.id-red:/{id_red}}")
    private String pathIdRed;

    @Value("${security.path.param.wildcard:/**}")
    private String pathWildcard;

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
        source.registerCorsConfiguration(pathWildcard, configuration);

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
                        .requestMatchers(apiInternal + pathWildcard).hasRole(roleService)
                        .requestMatchers(HttpMethod.GET, apiUsuarios + endpointDatosUsuario).hasRole(roleService)
                        .requestMatchers(HttpMethod.GET, apiUsuarios + endpointUsuarioExiste).hasRole(roleService)

                        // Recuperación de contraseña - Públicos
                        .requestMatchers(HttpMethod.POST, apiUsuarios + endpointRecuperarPassword).permitAll()
                        .requestMatchers(HttpMethod.POST, apiUsuarios + endpointRestablecerPassword).permitAll()

                        // Verificación de email - Públicos
                        .requestMatchers(HttpMethod.GET, apiUsuarios + endpointVerificarEmail).permitAll()
                        .requestMatchers(HttpMethod.POST, apiUsuarios + endpointReenviarVerificacion).permitAll()

                        // Endpoints públicos generales
                        .requestMatchers(HttpMethod.GET, apiPublic + pathWildcard).permitAll()

                        // Artistas - Consultas públicas
                        .requestMatchers(HttpMethod.GET, apiArtistas).permitAll()
                        .requestMatchers(HttpMethod.GET, apiArtistas + pathId).permitAll()
                        .requestMatchers(HttpMethod.GET, apiArtistas + pathId + endpointArtistasRedes).permitAll()

                        // Artistas - Métodos de cobro (requieren autenticación)
                        .requestMatchers(apiArtistas + pathId + endpointMetodosCobro + pathWildcard).authenticated()

                        // Artistas - Redes sociales (requieren autenticación)
                        .requestMatchers(HttpMethod.POST, apiArtistas + pathId + endpointArtistasRedes).authenticated()
                        .requestMatchers(HttpMethod.PUT, apiArtistas + pathId + endpointArtistasRedes + pathIdRed).authenticated()
                        .requestMatchers(HttpMethod.DELETE, apiArtistas + pathId + endpointArtistasRedes + pathIdRed).authenticated()

                        // Artistas - Gestión (requieren autenticación)
                        .requestMatchers(HttpMethod.POST, apiArtistas).authenticated()
                        .requestMatchers(HttpMethod.PUT, apiArtistas + pathId).authenticated()
                        .requestMatchers(HttpMethod.DELETE, apiArtistas + pathId).authenticated()
                        .requestMatchers(HttpMethod.POST, apiArtistas + pathId + endpointRenunciar).authenticated()

                        // Usuarios - Métodos de pago (requieren autenticación)
                        .requestMatchers(apiUsuarios + pathId + endpointMetodosPago + pathWildcard).authenticated()

                        // Usuarios - Gestión de perfil (requieren autenticación)
                        .requestMatchers(HttpMethod.GET, apiUsuarios + pathId).authenticated()
                        .requestMatchers(HttpMethod.PUT, apiUsuarios + pathId).authenticated()
                        .requestMatchers(HttpMethod.DELETE, apiUsuarios + pathId).authenticated()
                        .requestMatchers(HttpMethod.PATCH, apiUsuarios + pathId + endpointOnboarding).authenticated()
                        .requestMatchers(HttpMethod.PUT, apiUsuarios + pathId + endpointCambiarPassword).authenticated()
                        .requestMatchers(HttpMethod.POST, apiUsuarios + endpointUsuariosLogoutAll).authenticated()

                        // Seguimientos - Consultas públicas
                        .requestMatchers(HttpMethod.GET, apiSeguimientos + pathIdUsuario + endpointSeguidos).permitAll()
                        .requestMatchers(HttpMethod.GET, apiSeguimientos + pathIdUsuario + endpointSeguidores).permitAll()
                        .requestMatchers(HttpMethod.GET, apiSeguimientos + pathIdUsuario + endpointEstadisticas).permitAll()

                        // Seguimientos - Acciones (requieren autenticación)
                        .requestMatchers(HttpMethod.POST, apiSeguimientos).authenticated()
                        .requestMatchers(HttpMethod.DELETE, apiSeguimientos + pathIdUsuario).authenticated()
                        .requestMatchers(HttpMethod.GET, apiSeguimientos + pathIdUsuario + endpointVerificar).authenticated()

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