package com.ondra.users.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuración de Cloudinary para la gestión de archivos multimedia.
 *
 * <p>Esta clase configura el cliente de Cloudinary utilizando las credenciales
 * almacenadas en application.properties. Cloudinary se utiliza para almacenar
 * y gestionar imágenes de perfil de usuarios.</p>
 *
 */
@Configuration
public class CloudinaryConfig {

    /**
     * Nombre del cloud de Cloudinary obtenido desde las properties.
     */
    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    /**
     * API Key de Cloudinary para autenticación.
     */
    @Value("${cloudinary.api-key}")
    private String apiKey;

    /**
     * API Secret de Cloudinary para autenticación segura.
     */
    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    /**
     * Crea y configura el bean de Cloudinary.
     *
     * <p>Este bean se utiliza en toda la aplicación para realizar operaciones
     * de subida, eliminación y transformación de archivos multimedia en Cloudinary.</p>
     *
     * @return instancia configurada de {@link Cloudinary}
     * @throws IllegalArgumentException si alguna de las credenciales es nula o vacía
     */
    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        return new Cloudinary(config);
    }
}