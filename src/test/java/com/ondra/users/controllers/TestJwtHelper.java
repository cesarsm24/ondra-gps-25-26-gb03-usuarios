package com.ondra.users.controllers;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper para generar tokens JWT utilizados en tests unitarios.
 *
 * <p>
 * Proporciona métodos para crear tokens de prueba tanto para usuarios normales como para artistas,
 * incluyendo los claims necesarios como userId, email y tipoUsuario.
 * </p>
 */
@Slf4j
@Component
public class TestJwtHelper {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * Genera un token JWT de prueba para un usuario.
     *
     * @param userId ID del usuario
     * @param email  Email del usuario
     * @return Token JWT válido
     */
    public String generarTokenPrueba(Long userId, String email) {
        log.debug("🔑 Generando token JWT de prueba para userId={}, email={}", userId, email);
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("tipoUsuario", "NORMAL");

        String token = createToken(claims, email);
        log.debug("✅ Token generado correctamente");
        return token;
    }

    /**
     * Genera un token JWT de prueba para un artista.
     *
     * @param userId   ID del usuario
     * @param artistId ID del artista
     * @param email    Email del usuario
     * @return Token JWT válido
     */
    public String generarTokenPruebaArtista(Long userId, Long artistId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("artistId", artistId);
        claims.put("email", email);
        claims.put("tipoUsuario", "ARTISTA");

        return createToken(claims, email);
    }

    /**
     * Crea un token JWT con los claims y subject proporcionados.
     *
     * @param claims  Claims a incluir en el token
     * @param subject Subject del token (normalmente el email)
     * @return Token JWT firmado
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Obtiene la clave secreta para firmar los tokens JWT.
     *
     * @return SecretKey utilizada para la firma HMAC SHA
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
