package com.ondra.users.services;

import com.ondra.users.repositories.ArtistaRepository;
import com.ondra.users.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;

/**
 * Servicio para la generación de slugs únicos.
 *
 * <p>Proporciona métodos para crear identificadores únicos legibles (slugs)
 * para usuarios y artistas, normalizando texto y resolviendo colisiones.</p>
 *
 * <p>Estrategias de generación:</p>
 * <ul>
 *   <li>Usuarios: Combinaciones de nombre y apellidos con sufijos numéricos</li>
 *   <li>Artistas: Nombre artístico normalizado con sufijos numéricos</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SlugGeneratorService {

    private final UsuarioRepository usuarioRepository;
    private final ArtistaRepository artistaRepository;

    /** RNG seguro y reutilizado (cumple SonarCloud S2245 + S6544). */
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Genera un slug único para un usuario basado en nombre y apellidos.
     */
    public String generarSlugUsuario(String nombre, String apellidos) {

        String nombreLimpio = normalizarTexto(nombre != null ? nombre : "usuario");
        String apellidosLimpio = normalizarTexto(apellidos != null ? apellidos : "");

        String[] partesApellidos = apellidosLimpio.split("\\s+");
        String primerApellido = partesApellidos.length > 0 ? partesApellidos[0] : "";
        String segundoApellido = partesApellidos.length > 1 ? partesApellidos[1] : "";

        List<String> variaciones = new ArrayList<>();

        if (!primerApellido.isEmpty()) {
            variaciones.add(nombreLimpio.charAt(0) + primerApellido);
            variaciones.add(nombreLimpio + primerApellido.charAt(0));
            variaciones.add(nombreLimpio + primerApellido);
        }

        if (!primerApellido.isEmpty() && !segundoApellido.isEmpty()) {
            variaciones.add(nombreLimpio.charAt(0) + primerApellido.charAt(0) + segundoApellido);
        }

        if (!segundoApellido.isEmpty()) {
            variaciones.add(nombreLimpio.charAt(0) + segundoApellido);
        }

        if (nombreLimpio.length() >= 2 && !primerApellido.isEmpty()) {
            variaciones.add(nombreLimpio.substring(0, 2) + primerApellido);
        }

        if (variaciones.isEmpty()) {
            variaciones.add(nombreLimpio);
        }

        Collections.shuffle(variaciones, RANDOM);

        for (String variacion : variaciones) {
            if (!usuarioRepository.existsBySlug(variacion)) {
                log.debug("🏷️ Slug de usuario generado: {}", variacion);
                return variacion;
            }

            // Intentos con sufijo numérico
            for (int i = 0; i < 5; i++) {
                int sufijo = 1 + RANDOM.nextInt(999);
                String slugConSufijo = variacion + sufijo;

                if (!usuarioRepository.existsBySlug(slugConSufijo)) {
                    log.debug("🏷️ Slug de usuario generado (con sufijo): {}", slugConSufijo);
                    return slugConSufijo;
                }
            }
        }

        // Fallback usando UUID
        String slugFinal = nombreLimpio + UUID.randomUUID().toString().substring(0, 6);
        log.warn("Slug de usuario generado con UUID por alta colisión: {}", slugFinal);
        return slugFinal;
    }

    /**
     * Genera un slug único para un artista basado en su nombre artístico.
     */
    public String generarSlugArtista(String nombreArtistico) {

        String slugBase = normalizarTexto(nombreArtistico);

        if (slugBase.isEmpty()) {
            slugBase = "artista";
        }

        String slugFinal = slugBase;
        int intentos = 0;

        while (artistaRepository.existsBySlugArtistico(slugFinal) && intentos < 10) {
            int sufijo = 1 + RANDOM.nextInt(99);
            slugFinal = slugBase + sufijo;
            intentos++;
        }

        if (artistaRepository.existsBySlugArtistico(slugFinal)) {
            slugFinal = slugBase + UUID.randomUUID().toString().substring(0, 6);
            log.warn("Slug de artista generado con UUID: {}", slugFinal);
        }

        log.debug("🏷️ Slug de artista generado: {}", slugFinal);
        return slugFinal;
    }

    /**
     * Normaliza texto para uso en slugs.
     */
    private String normalizarTexto(String texto) {

        if (texto == null || texto.isEmpty()) {
            return "";
        }

        return texto.toLowerCase()
                .replaceAll("[áàäâ]", "a")
                .replaceAll("[éèëê]", "e")
                .replaceAll("[íìïî]", "i")
                .replaceAll("[óòöô]", "o")
                .replaceAll("[úùüû]", "u")
                .replaceAll("ñ", "n")
                .replaceAll("[çć]", "c")
                .replaceAll("\\s+", "")
                .replaceAll("[^a-z0-9]", "");
    }
}