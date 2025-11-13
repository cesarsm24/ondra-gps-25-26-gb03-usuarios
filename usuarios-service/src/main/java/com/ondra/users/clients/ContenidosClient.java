package com.ondra.users.clients;

import com.ondra.users.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Cliente HTTP para comunicarse con el microservicio de Contenidos.
 *
 * <p>Maneja todas las operaciones relacionadas con compras, favoritos, comentarios,
 * canciones y álbumes del microservicio de Contenidos.</p>
 *
 * <p><strong>IMPORTANTE:</strong> Las llamadas entre microservicios deben incluir
 * un token de servicio en el header para autenticación.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContenidosClient {

    private final RestTemplate restTemplate;

    @Value("${microservices.contenidos.url}")
    private String contenidosServiceUrl;

    @Value("${microservices.service-token}")
    private String serviceToken;

    /**
     * Obtiene las compras de un usuario (canciones y álbumes).
     *
     * @param idUsuario ID del usuario
     * @return ComprasResumenDTO con las compras del usuario
     */
    public ComprasResumenDTO obtenerComprasUsuario(Long idUsuario) {
        try {
            String url = contenidosServiceUrl + "/usuarios/" + idUsuario + "/compras";
            log.debug("Llamando a microservicio Contenidos: GET {}", url);

            HttpHeaders headers = createServiceHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<ComprasResumenDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    ComprasResumenDTO.class
            );

            log.info("Compras obtenidas exitosamente para usuario ID: {}", idUsuario);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("No se encontraron compras para usuario ID: {}", idUsuario);
            return ComprasResumenDTO.builder()
                    .totalCompras(0)
                    .totalGastado(0.0)
                    .canciones(java.util.Collections.emptyList())
                    .albumes(java.util.Collections.emptyList())
                    .build();
        } catch (Exception e) {
            log.error("Error al obtener compras del usuario ID {}: {}", idUsuario, e.getMessage());
            return ComprasResumenDTO.builder()
                    .totalCompras(0)
                    .totalGastado(0.0)
                    .canciones(java.util.Collections.emptyList())
                    .albumes(java.util.Collections.emptyList())
                    .build();
        }
    }

    /**
     * Obtiene los favoritos de un usuario (canciones y álbumes).
     *
     * @param idUsuario ID del usuario
     * @return FavoritosResumenDTO con los favoritos del usuario
     */
    public FavoritosResumenDTO obtenerFavoritosUsuario(Long idUsuario) {
        try {
            String url = contenidosServiceUrl + "/usuarios/" + idUsuario + "/favoritos";
            log.debug("Llamando a microservicio Contenidos: GET {}", url);

            HttpHeaders headers = createServiceHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<FavoritosResumenDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    FavoritosResumenDTO.class
            );

            log.info("Favoritos obtenidos exitosamente para usuario ID: {}", idUsuario);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("No se encontraron favoritos para usuario ID: {}", idUsuario);
            return FavoritosResumenDTO.builder()
                    .totalFavoritos(0)
                    .canciones(java.util.Collections.emptyList())
                    .albumes(java.util.Collections.emptyList())
                    .build();
        } catch (Exception e) {
            log.error("Error al obtener favoritos del usuario ID {}: {}", idUsuario, e.getMessage());
            return FavoritosResumenDTO.builder()
                    .totalFavoritos(0)
                    .canciones(java.util.Collections.emptyList())
                    .albumes(java.util.Collections.emptyList())
                    .build();
        }
    }

    /**
     * Elimina todos los comentarios de un usuario.
     *
     * @param idUsuario ID del usuario
     */
    public void eliminarComentariosUsuario(Long idUsuario) {
        try {
            String url = contenidosServiceUrl + "/usuarios/" + idUsuario + "/comentarios";
            log.debug("Llamando a microservicio Contenidos: DELETE {}", url);

            HttpHeaders headers = createServiceHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
            log.info("Comentarios eliminados para usuario ID: {}", idUsuario);
        } catch (Exception e) {
            log.error("Error al eliminar comentarios del usuario ID {}: {}", idUsuario, e.getMessage());
        }
    }

    /**
     * Elimina todas las compras de un usuario.
     *
     * @param idUsuario ID del usuario
     */
    public void eliminarComprasUsuario(Long idUsuario) {
        try {
            String url = contenidosServiceUrl + "/usuarios/" + idUsuario + "/ventas";
            log.debug("Llamando a microservicio Contenidos: DELETE {}", url);

            HttpHeaders headers = createServiceHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
            log.info("Compras eliminadas para usuario ID: {}", idUsuario);
        } catch (Exception e) {
            log.error("Error al eliminar compras del usuario ID {}: {}", idUsuario, e.getMessage());
            // No lanzamos excepción para no interrumpir la eliminación del usuario
        }
    }

    /**
     * Elimina todos los favoritos de un usuario.
     *
     * @param idUsuario ID del usuario
     */
    public void eliminarFavoritosUsuario(Long idUsuario) {
        try {
            String url = contenidosServiceUrl + "/usuarios/" + idUsuario + "/favoritos";
            log.debug("Llamando a microservicio Contenidos: DELETE {}", url);

            HttpHeaders headers = createServiceHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
            log.info("Favoritos eliminados para usuario ID: {}", idUsuario);
        } catch (Exception e) {
            log.error("Error al eliminar favoritos del usuario ID {}: {}", idUsuario, e.getMessage());
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