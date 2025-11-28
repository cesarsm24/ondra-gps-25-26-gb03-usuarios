package com.ondra.users.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * Filtro encargado de la validación de tokens JWT en las peticiones protegidas.
 *
 * <p>Analiza el encabezado Authorization de cada solicitud, extrae el token,
 * lo valida y, en caso de ser correcto, establece el contexto de autenticación
 * en Spring Security.</p>
 *
 * <p>Las rutas públicas definidas en {@link #shouldNotFilter(HttpServletRequest)}
 * quedan excluidas del proceso de validación.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secretKey;

    private static final String[] PUBLIC_PATTERNS = {
            "/api/usuarios/login",
            "/api/usuarios/login/google",
            "/api/usuarios/refresh",
            "/api/usuarios/logout",
            "/api/usuarios/recuperar-password",
            "/api/usuarios/restablecer-password",
            "/api/usuarios/verificar-email",
            "/api/usuarios/reenviar-verificacion",
            "/api/public/",
            "/actuator/health"
    };

    /**
     * Determina si una petición debe omitirse del proceso de validación JWT.
     *
     * @param request petición HTTP
     * @return true si la ruta es pública, false en caso contrario
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if ("POST".equals(method) && path.equals("/api/usuarios")) {
            return true;
        }

        if ("GET".equals(method)) {
            if (path.equals("/api/usuarios/stats")) {
                return true;
            }

            if (path.startsWith("/api/public/")) {
                return true;
            }

            if (path.equals("/api/artistas") || path.startsWith("/api/artistas?")) {
                return true;
            }

            if (path.matches("/api/artistas/\\d+/?$")) {
                return true;
            }

            if (path.matches("/api/artistas/\\d+/redes")) {
                return true;
            }

            if (path.matches("/api/seguimientos/\\d+/seguidos")
                    || path.matches("/api/seguimientos/\\d+/seguidores")
                    || path.matches("/api/seguimientos/\\d+/estadisticas")) {
                return true;
            }
        }

        if (path.equals("/api/usuarios/login") ||
                path.equals("/api/usuarios/login/google") ||
                path.equals("/api/usuarios/refresh") ||
                path.equals("/api/usuarios/recuperar-password") ||
                path.equals("/api/usuarios/restablecer-password") ||
                path.startsWith("/api/usuarios/verificar-email") ||
                path.equals("/api/usuarios/reenviar-verificacion") ||
                path.startsWith("/api/public/") ||
                path.equals("/actuator/health")) {
            return true;
        }

        return false;
    }

    /**
     * Procesa la validación del token JWT y establece la autenticación en el contexto
     * de seguridad cuando el token es válido.
     *
     * @param request petición HTTP
     * @param response respuesta HTTP
     * @param filterChain cadena de filtros de Spring Security
     * @throws ServletException si ocurre un error en el procesamiento del filtro
     * @throws IOException si ocurre un error de entrada/salida
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = extractTokenFromRequest(request);

            if (token != null && validateToken(token)) {
                String userId = extractUserIdFromToken(token);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                Collections.emptyList()
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Usuario {} autenticado correctamente", userId);
            }

        } catch (ExpiredJwtException e) {
            log.warn("Token expirado: {}", e.getMessage());
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "TOKEN_EXPIRED", "El token ha expirado");
            return;

        } catch (SignatureException | MalformedJwtException e) {
            log.warn("Token inválido: {}", e.getMessage());
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "INVALID_TOKEN", "Token inválido");
            return;

        } catch (UnsupportedJwtException | IllegalArgumentException e) {
            log.warn("Token no soportado: {}", e.getMessage());
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "INVALID_TOKEN", "Token inválido");
            return;

        } catch (Exception e) {
            log.error("Error inesperado validando JWT", e);
            writeErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "INTERNAL_ERROR", "Error al procesar el token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token JWT del encabezado Authorization.
     *
     * @param request petición HTTP
     * @return token sin el prefijo "Bearer ", o null si no está presente
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Valida la firma y estructura del token proporcionado.
     *
     * @param token token JWT
     * @return true si el token es válido
     * @throws ExpiredJwtException si el token ha expirado
     * @throws SignatureException si la firma es incorrecta
     * @throws MalformedJwtException si el token tiene un formato inválido
     */
    private boolean validateToken(String token) {
        Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
        return true;
    }

    /**
     * Obtiene el identificador de usuario incluido en el token.
     *
     * @param token token JWT validado
     * @return identificador del usuario
     * @throws IllegalArgumentException si no existe el campo userId
     */
    private String extractUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Object userIdObj = claims.get("userId");
        if (userIdObj == null) {
            throw new IllegalArgumentException("El token no contiene el campo userId");
        }

        return String.valueOf(userIdObj);
    }

    /**
     * Obtiene el identificador del artista incluido en el token.
     *
     * @param token token JWT validado
     * @return identificador del artista
     * @throws IllegalArgumentException si no existe el campo artistId
     */
    private String extractArtistIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Object artistIdObj = claims.get("artistId");

        if (artistIdObj == null) {
            throw new IllegalArgumentException("El token no contiene el campo artistId");
        }

        return String.valueOf(artistIdObj);
    }

    /**
     * Genera la clave secreta utilizada para validar la firma de los tokens JWT.
     *
     * @return clave secreta HMAC
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Envía una respuesta de error en formato JSON.
     *
     * @param response respuesta HTTP
     * @param status código de estado HTTP
     * @param error código de error
     * @param message mensaje descriptivo
     * @throws IOException si ocurre un error al escribir la respuesta
     */
    private void writeErrorResponse(HttpServletResponse response, int status,
                                    String error, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format(
                "{\"error\":\"%s\",\"message\":\"%s\"}", error, message
        ));
    }
}
