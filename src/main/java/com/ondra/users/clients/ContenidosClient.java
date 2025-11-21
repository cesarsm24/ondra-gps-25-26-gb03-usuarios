package com.ondra.users.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Cliente HTTP para comunicación con el microservicio de Contenidos.
 *
 * <p>Gestiona operaciones relacionadas con compras, favoritos, carrito, comentarios,
 * canciones y álbumes. Todas las peticiones incluyen autenticación mediante
 * token de servicio en el header.</p>
 *
 * <p>Los métodos de eliminación están diseñados para ser llamados cuando se elimina
 * un usuario del sistema, garantizando la eliminación en cascada de todos sus datos
 * en el microservicio de contenidos.</p>
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
     * Elimina todas las compras asociadas a un usuario.
     *
     * @param idUsuario ID del usuario
     */
    public void eliminarComprasUsuario(Long idUsuario) {
        try {
            String url = contenidosServiceUrl + "/compras/usuarios/" + idUsuario;
            log.debug("🗑️ Eliminando compras del usuario {}", idUsuario);

            HttpHeaders headers = createServiceHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
            log.info("✅ Compras eliminadas para usuario {}", idUsuario);
        } catch (Exception e) {
            log.error("❌ Error al eliminar compras del usuario {}: {}", idUsuario, e.getMessage());
        }
    }

    /**
     * Elimina todos los favoritos asociados a un usuario.
     *
     * @param idUsuario ID del usuario
     */
    public void eliminarFavoritosUsuario(Long idUsuario) {
        try {
            String url = contenidosServiceUrl + "/favoritos/usuarios/" + idUsuario;
            log.debug("🗑️ Eliminando favoritos del usuario {}", idUsuario);

            HttpHeaders headers = createServiceHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
            log.info("✅ Favoritos eliminados para usuario {}", idUsuario);
        } catch (Exception e) {
            log.error("❌ Error al eliminar favoritos del usuario {}: {}", idUsuario, e.getMessage());
        }
    }

    /**
     * Elimina todos los comentarios asociados a un usuario.
     *
     * @param idUsuario ID del usuario
     */
    public void eliminarComentariosUsuario(Long idUsuario) {
        try {
            String url = contenidosServiceUrl + "/comentarios/usuarios/" + idUsuario;
            log.debug("🗑️ Eliminando comentarios del usuario {}", idUsuario);

            HttpHeaders headers = createServiceHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
            log.info("✅ Comentarios eliminados para usuario {}", idUsuario);
        } catch (Exception e) {
            log.error("❌ Error al eliminar comentarios del usuario {}: {}", idUsuario, e.getMessage());
        }
    }

    /**
     * Elimina todas las valoraciones asociadas a un usuario.
     *
     * @param idUsuario ID del usuario
     */
    public void eliminarValoracionesUsuario(Long idUsuario) {
        try {
            String url = contenidosServiceUrl + "/valoraciones/usuarios/" + idUsuario;
            log.debug("🗑️ Eliminando valoraciones del usuario {}", idUsuario);

            HttpHeaders headers = createServiceHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
            log.info("✅ Valoraciones eliminadas para usuario {}", idUsuario);
        } catch (Exception e) {
            log.error("❌ Error al eliminar valoraciones del usuario {}: {}", idUsuario, e.getMessage());
        }
    }

    /**
     * Elimina el carrito asociado a un usuario.
     *
     * @param idUsuario ID del usuario
     */
    public void eliminarCarritoUsuario(Long idUsuario) {
        try {
            String url = contenidosServiceUrl + "/carrito/usuarios/" + idUsuario;
            log.debug("🗑️ Eliminando carrito del usuario {}", idUsuario);

            HttpHeaders headers = createServiceHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
            log.info("✅ Carrito eliminado para usuario {}", idUsuario);
        } catch (Exception e) {
            log.error("❌ Error al eliminar carrito del usuario {}: {}", idUsuario, e.getMessage());
        }
    }

    /**
     * Elimina todos los álbumes de un artista.
     *
     * <p>Debe invocarse antes de eliminarCancionesArtista() debido a
     * restricciones de clave foránea.</p>
     *
     * @param idArtista ID del artista
     */
    public void eliminarAlbumesArtista(Long idArtista) {
        try {
            String url = contenidosServiceUrl + "/albumes/artist/" + idArtista;
            log.debug("🗑️ Eliminando álbumes del artista {}", idArtista);

            HttpHeaders headers = createServiceHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
            log.info("✅ Álbumes eliminados para artista {}", idArtista);
        } catch (Exception e) {
            log.error("❌ Error al eliminar álbumes del artista {}: {}", idArtista, e.getMessage());
        }
    }

    /**
     * Elimina todas las canciones de un artista.
     *
     * <p>Debe invocarse después de eliminarAlbumesArtista() debido a
     * restricciones de clave foránea.</p>
     *
     * @param idArtista ID del artista
     */
    public void eliminarCancionesArtista(Long idArtista) {
        try {
            String url = contenidosServiceUrl + "/canciones/artist/" + idArtista;
            log.debug("🗑️ Eliminando canciones del artista {}", idArtista);

            HttpHeaders headers = createServiceHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
            log.info("✅ Canciones eliminadas para artista {}", idArtista);
        } catch (Exception e) {
            log.error("❌ Error al eliminar canciones del artista {}: {}", idArtista, e.getMessage());
        }
    }

    /**
     * Crea headers HTTP con autenticación de servicio.
     *
     * @return HttpHeaders configurados con token de servicio
     */
    private HttpHeaders createServiceHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Service-Token", serviceToken);
        return headers;
    }
}