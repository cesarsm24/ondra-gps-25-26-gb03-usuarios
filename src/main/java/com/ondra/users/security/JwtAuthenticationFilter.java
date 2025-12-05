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
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secretKey;
    
    private static final String PUBLIC_API_PATH = "/api/public/";

    private static final String[] PUBLIC_PATTERNS = {
            "/api/usuarios/login",
            "/api/usuarios/login/google",
            "/api/usuarios/refresh",
            "/api/usuarios/logout",
            "/api/usuarios/recuperar-password",
            "/api/usuarios/restablecer-password",
            "/api/usuarios/verificar-email",
            "/api/usuarios/reenviar-verificacion",
            PUBLIC_API_PATH,
            "/actuator/health"
    };

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if (isPostUserRegistration(method, path)) {
            return true;
        }

        if (isPublicGetEndpoint(method, path)) {
            return true;
        }

        return isPublicEndpoint(path);
    }

    private boolean isPostUserRegistration(String method, String path) {
        return "POST".equals(method) && path.equals("/api/usuarios");
    }

    private boolean isPublicGetEndpoint(String method, String path) {
        if (!"GET".equals(method)) {
            return false;
        }

        return path.equals("/api/usuarios/stats")
                || path.startsWith(PUBLIC_API_PATH)
                || isPublicArtistEndpoint(path)
                || isPublicFollowEndpoint(path);
    }

    private boolean isPublicArtistEndpoint(String path) {
        return path.equals("/api/artistas")
                || path.startsWith("/api/artistas?")
                || path.matches("/api/artistas/\\d+/?$")
                || path.matches("/api/artistas/\\d+/redes");
    }

    private boolean isPublicFollowEndpoint(String path) {
        return path.matches("/api/seguimientos/\\d+/seguidos")
                || path.matches("/api/seguimientos/\\d+/seguidores")
                || path.matches("/api/seguimientos/\\d+/estadisticas");
    }

    private boolean isPublicEndpoint(String path) {
        return path.equals("/api/usuarios/login")
                || path.equals("/api/usuarios/login/google")
                || path.equals("/api/usuarios/refresh")
                || path.equals("/api/usuarios/recuperar-password")
                || path.equals("/api/usuarios/restablecer-password")
                || path.startsWith("/api/usuarios/verificar-email")
                || path.equals("/api/usuarios/reenviar-verificacion")
                || path.startsWith(PUBLIC_API_PATH)
                || path.equals("/actuator/health");
    }

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

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // Solución Code Smell 3: Simplificar con return directo
        return (bearerToken != null && bearerToken.startsWith("Bearer "))
                ? bearerToken.substring(7)
                : null;
    }

    private boolean validateToken(String token) {
        Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
        return true;
    }

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

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

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