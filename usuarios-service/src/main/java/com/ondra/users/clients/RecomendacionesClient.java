package com.ondra.users.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Cliente HTTP para comunicarse con el microservicio de Recomendaciones.
 *
 * <p>Maneja las operaciones relacionadas con preferencias de géneros musicales.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecomendacionesClient {

    private final RestTemplate restTemplate;

    @Value("${microservices.recomendaciones.url}")
    private String recomendacionesServiceUrl;

    @Value("${microservices.service-token}")
    private String serviceToken;

    /**
     * Elimina todas las preferencias de géneros de un usuario.
     *
     * @param idUsuario ID del usuario
     */
    public void eliminarPreferenciasUsuario(Long idUsuario) {
        try {
            String url = recomendacionesServiceUrl + "/usuarios/" + idUsuario + "/preferencias";
            log.debug("Llamando a microservicio Recomendaciones: DELETE {}", url);

            HttpHeaders headers = createServiceHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
            log.info("Preferencias eliminadas para usuario ID: {}", idUsuario);
        } catch (Exception e) {
            log.error("Error al eliminar preferencias del usuario ID {}: {}", idUsuario, e.getMessage());
            // No lanzamos excepción para no interrumpir la eliminación del usuario
        }
    }

    /**
     * Crea los headers necesarios para comunicación entre servicios.
     *
     * @return HttpHeaders con el token de servicio
     */
    private HttpHeaders createServiceHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Service-Token", serviceToken);
        return headers;
    }
}