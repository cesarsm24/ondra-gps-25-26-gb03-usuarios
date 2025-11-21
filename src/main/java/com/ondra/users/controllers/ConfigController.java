package com.ondra.users.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para configuración pública de la aplicación.
 *
 * <p>Expone valores de configuración necesarios para el cliente frontend,
 * como credenciales OAuth y datos de la aplicación. No requiere autenticación.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/config")
@CrossOrigin(origins = "*")
public class ConfigController {

    @Value("${google.oauth.client-id:}")
    private String googleClientId;

    @Value("${spring.application.name:OndraSounds}")
    private String appName;

    /**
     * Obtiene la configuración pública de la aplicación.
     *
     * <p>Proporciona valores seguros como el Google Client ID para OAuth
     * y el nombre de la aplicación.</p>
     *
     * @return Mapa con configuración pública
     */
    @GetMapping("/public")
    public ResponseEntity<Map<String, String>> obtenerConfigPublica() {
        log.debug("📋 Obteniendo configuración pública");

        Map<String, String> config = new HashMap<>();
        config.put("googleClientId", googleClientId);
        config.put("appName", appName);

        return ResponseEntity.ok(config);
    }
}