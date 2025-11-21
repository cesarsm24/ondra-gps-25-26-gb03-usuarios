package com.ondra.users.security;

import com.ondra.users.exceptions.InvalidRefreshTokenException;
import com.ondra.users.models.dao.RefreshToken;
import com.ondra.users.models.dao.Usuario;
import com.ondra.users.repositories.RefreshTokenRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio para la generación y gestión de tokens JWT y refresh tokens.
 *
 * <p>Proporciona operaciones para crear tokens de acceso y refresco,
 * validar tokens existentes y gestionar su ciclo de vida incluyendo
 * revocación y limpieza de tokens expirados.</p>
 *
 * <p>Compatible con jjwt 0.12.5. La validación de tokens de acceso
 * se realiza en {@link JwtAuthenticationFilter}.</p>
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh.expiration}")
    private Long refreshExpiration;

    /**
     * Genera un token JWT de acceso para un usuario autenticado.
     *
     * @param email email del usuario
     * @param userId identificador del usuario
     * @param artistId identificador del artista, puede ser null
     * @return token JWT firmado
     */
    public String generarToken(String email, Long userId, Long artistId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);

        if (artistId != null) {
            claims.put("artistId", artistId);
        }

        return createToken(claims, email);
    }

    /**
     * Genera un token JWT de acceso incluyendo el tipo de usuario.
     *
     * @param email email del usuario
     * @param userId identificador del usuario
     * @param tipoUsuario tipo de usuario
     * @param artistId identificador del artista, puede ser null
     * @return token JWT firmado
     */
    public String generarToken(String email, Long userId, String tipoUsuario, Long artistId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("tipoUsuario", tipoUsuario);

        if (artistId != null) {
            claims.put("artistId", artistId);
        }

        return createToken(claims, email);
    }

    /**
     * Genera y almacena un token de refresco para un usuario.
     *
     * @param usuario usuario para el que se genera el token
     * @return token de refresco almacenado
     */
    @Transactional
    public RefreshToken generarRefreshToken(Usuario usuario) {
        String token = UUID.randomUUID().toString();

        LocalDateTime fechaExpiracion = LocalDateTime.now()
                .plusSeconds(refreshExpiration / 1000);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .usuario(usuario)
                .fechaExpiracion(fechaExpiracion)
                .revocado(false)
                .fechaCreacion(LocalDateTime.now())
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Valida un token de refresco verificando su existencia, expiración y estado.
     *
     * @param token token a validar
     * @return token de refresco validado
     * @throws InvalidRefreshTokenException si el token es inválido o ha expirado
     */
    @Transactional(readOnly = true)
    public RefreshToken validarRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token inválido o expirado"));

        if (!refreshToken.esValido()) {
            throw new InvalidRefreshTokenException("Refresh token inválido o expirado");
        }

        return refreshToken;
    }

    /**
     * Revoca un token de refresco específico.
     *
     * @param token token a revocar
     */
    @Transactional
    public void revocarRefreshToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevocado(true);
            refreshTokenRepository.save(rt);
        });
    }

    /**
     * Revoca todos los tokens de refresco de un usuario.
     *
     * @param idUsuario identificador del usuario
     */
    @Transactional
    public void revocarTodosLosTokensDelUsuario(Long idUsuario) {
        refreshTokenRepository.findByUsuario_IdUsuario(idUsuario)
                .forEach(rt -> {
                    rt.setRevocado(true);
                    refreshTokenRepository.save(rt);
                });
    }

    /**
     * Elimina tokens de refresco expirados de la base de datos.
     */
    @Transactional
    public void limpiarTokensExpirados() {
        refreshTokenRepository.eliminarTokensExpirados(LocalDateTime.now());
    }

    /**
     * Crea un token JWT con los claims proporcionados.
     *
     * @param claims datos a incluir en el token
     * @param subject identificador principal del token
     * @return token JWT firmado
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
     * Genera la clave secreta para firmar tokens JWT.
     *
     * @return clave secreta HMAC
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}