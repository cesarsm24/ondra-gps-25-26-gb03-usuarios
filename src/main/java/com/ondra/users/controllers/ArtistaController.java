package com.ondra.users.controllers;

import com.ondra.users.dto.ArtistaDTO;
import com.ondra.users.dto.CrearArtistaDTO;
import com.ondra.users.dto.EditarArtistaDTO;
import com.ondra.users.dto.SuccessfulResponseDTO;
import com.ondra.users.services.ArtistaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para la gestión de perfiles de artistas.
 * Proporciona operaciones de consulta, creación, edición y eliminación.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ArtistaController {

    private final ArtistaService artistaService;

    /**
     * Obtiene artistas en tendencia.
     *
     * @param limit número máximo de resultados
     * @return lista de artistas en tendencia
     */
    @GetMapping(value = "/artistas", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ArtistaDTO>> listarArtistasTendencia(
            @RequestParam(defaultValue = "5") int limit) {

        List<ArtistaDTO> artistas = artistaService.listarArtistasTendencia(limit);
        return ResponseEntity.ok(artistas);
    }

    /**
     * Busca artistas mediante filtros opcionales.
     *
     * @param search nombre artístico o fragmento
     * @param esTendencia indicador de tendencia
     * @param orderBy criterio de ordenación
     * @param page número de página
     * @param limit tamaño de página
     * @return página de resultados
     */
    @GetMapping(value = "/artistas/buscar", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<ArtistaDTO>> buscarArtistas(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean esTendencia,
            @RequestParam(defaultValue = "most_recent") String orderBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit) {

        if (limit > 100) {
            limit = 100;
        }

        Page<ArtistaDTO> artistas = artistaService.buscarArtistas(
                search, esTendencia, orderBy, page, limit
        );

        return ResponseEntity.ok(artistas);
    }

    /**
     * Crea un perfil artístico para el usuario autenticado.
     *
     * @param crearArtistaDTO datos del artista
     * @param foto imagen asociada
     * @param authentication autenticación del usuario
     * @return artista creado
     */
    @PostMapping(value = "/convertirse-artista", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArtistaDTO> convertirseEnArtista(
            @RequestPart("datos") CrearArtistaDTO crearArtistaDTO,
            @RequestPart(value = "foto", required = false) MultipartFile foto,
            Authentication authentication) {

        Long authenticatedUserId = Long.parseLong(authentication.getName());

        ArtistaDTO artista = artistaService.convertirseEnArtista(
                crearArtistaDTO, foto, authenticatedUserId
        );

        return ResponseEntity.ok(artista);
    }

    /**
     * Permite renunciar al perfil artístico.
     *
     * @param id identificador del artista
     * @param authentication autenticación del usuario
     * @return confirmación de la operación
     */
    @PostMapping("/artistas/{id}/renunciar")
    public ResponseEntity<SuccessfulResponseDTO> renunciarPerfilArtista(
            @PathVariable Long id,
            Authentication authentication) {

        Long authenticatedUserId = Long.parseLong(authentication.getName());
        artistaService.renunciarPerfilArtista(id, authenticatedUserId);

        SuccessfulResponseDTO response = SuccessfulResponseDTO.builder()
                .successful("Renuncia de perfil artístico exitosa")
                .message("Se ha revertido el perfil a usuario estándar")
                .statusCode(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene un artista por su identificador.
     *
     * @param id identificador del artista
     * @return datos del artista
     */
    @GetMapping(value = "/artistas/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ArtistaDTO> obtenerArtista(@PathVariable Long id) {
        ArtistaDTO artista = artistaService.obtenerArtista(id);
        return ResponseEntity.ok(artista);
    }

    /**
     * Actualiza el perfil artístico.
     *
     * @param id identificador del artista
     * @param editarDTO datos a modificar
     * @param authentication autenticación del usuario
     * @return artista actualizado
     */
    @PutMapping(value = "/artistas/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ArtistaDTO> editarArtista(
            @PathVariable Long id,
            @Valid @RequestBody EditarArtistaDTO editarDTO,
            Authentication authentication) {

        Long authenticatedUserId = Long.parseLong(authentication.getName());

        ArtistaDTO artista = artistaService.editarArtista(id, editarDTO, authenticatedUserId);
        return ResponseEntity.ok(artista);
    }

    /**
     * Elimina un perfil artístico y marca la cuenta como inactiva.
     *
     * @param id identificador del artista
     * @param authentication autenticación del usuario
     * @return confirmación de eliminación
     */
    @DeleteMapping("/artistas/{id}")
    public ResponseEntity<SuccessfulResponseDTO> eliminarArtista(
            @PathVariable Long id,
            Authentication authentication) {

        Long authenticatedUserId = Long.parseLong(authentication.getName());
        artistaService.eliminarArtista(id, authenticatedUserId);

        SuccessfulResponseDTO response = SuccessfulResponseDTO.builder()
                .successful("Eliminación realizada")
                .message("El perfil artístico ha sido eliminado correctamente")
                .statusCode(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.ok(response);
    }
}
