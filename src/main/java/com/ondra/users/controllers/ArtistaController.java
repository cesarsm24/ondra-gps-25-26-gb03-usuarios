package com.ondra.users.controllers;

import com.ondra.users.dto.ArtistaDTO;
import com.ondra.users.dto.CrearArtistaDTO;
import com.ondra.users.dto.EditarArtistaDTO;
import com.ondra.users.dto.SuccessfulResponseDTO;
import com.ondra.users.models.dao.Usuario;
import com.ondra.users.services.ArtistaService;
import com.ondra.users.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para gestión de perfiles de artistas.
 *
 * <p>Proporciona endpoints para crear, consultar, editar y eliminar perfiles artísticos.
 * Los artistas son usuarios con capacidades extendidas para publicar contenido musical.</p>
 *
 * <p>Endpoints públicos: listado de tendencias y consulta de perfiles.</p>
 * <p>Endpoints protegidos: creación, edición, renuncia y eliminación de perfiles.</p>
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ArtistaController {

    private final ArtistaService artistaService;

    /**
     * Lista artistas marcados como tendencia.
     *
     * @param limit Número máximo de resultados (por defecto 5, máximo 20)
     * @return Lista de artistas en tendencia
     */
    @GetMapping(value = "/artistas", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ArtistaDTO>> listarArtistasTendencia(
            @RequestParam(defaultValue = "5") int limit) {

        List<ArtistaDTO> artistas = artistaService.listarArtistasTendencia(limit);
        return ResponseEntity.ok(artistas);
    }

    /**
     * Crea un perfil de artista para el usuario autenticado.
     *
     * <p>Convierte un usuario normal en artista, permitiéndole publicar contenido musical.</p>
     *
     * @param crearArtistaDTO Datos del perfil artístico
     * @param foto Imagen de perfil (opcional)
     * @param authentication Autenticación del usuario
     * @return Perfil de artista creado
     */
    @PostMapping(value = "/convertirse-artista", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArtistaDTO> convertirseEnArtista(
            @RequestPart("datos") CrearArtistaDTO crearArtistaDTO,
            @RequestPart(value = "foto", required = false) MultipartFile foto,
            Authentication authentication) {

        Long authenticatedUserId = Long.parseLong(authentication.getName());

        ArtistaDTO artista = artistaService.convertirseEnArtista(
                crearArtistaDTO,
                foto,
                authenticatedUserId
        );

        return ResponseEntity.ok(artista);
    }

    /**
     * Permite a un artista renunciar a su perfil y volver a ser usuario normal.
     *
     * <p>Elimina el perfil artístico, foto, redes sociales y métodos de cobro.
     * El usuario mantiene su cuenta activa como usuario normal.</p>
     *
     * @param id ID del artista
     * @param authentication Autenticación del usuario
     * @return Confirmación de la operación
     */
    @PostMapping("/artistas/{id}/renunciar")
    public ResponseEntity<SuccessfulResponseDTO> renunciarPerfilArtista(
            @PathVariable Long id,
            Authentication authentication) {

        Long authenticatedUserId = Long.parseLong(authentication.getName());

        artistaService.renunciarPerfilArtista(id, authenticatedUserId);

        SuccessfulResponseDTO response = SuccessfulResponseDTO.builder()
                .successful("Renuncia de perfil artístico exitosa")
                .message("Has dejado de ser artista y ahora eres un usuario normal. Tu cuenta permanece activa.")
                .statusCode(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene el perfil completo de un artista.
     *
     * @param id ID del artista
     * @return Datos del perfil artístico
     */
    @GetMapping(value = "/artistas/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ArtistaDTO> obtenerArtista(@PathVariable Long id) {
        ArtistaDTO artista = artistaService.obtenerArtista(id);
        return ResponseEntity.ok(artista);
    }

    /**
     * Actualiza los datos del perfil de un artista.
     *
     * <p>Solo el propietario puede editar su perfil. Si se proporciona una nueva foto,
     * la anterior se elimina automáticamente de Cloudinary.</p>
     *
     * @param id ID del artista
     * @param editarDTO Datos de actualización
     * @param authentication Autenticación del usuario
     * @return Perfil actualizado
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
     * Elimina el perfil de un artista y marca su cuenta como inactiva.
     *
     * <p>Elimina el perfil artístico, foto, redes sociales y métodos de cobro.
     * La cuenta de usuario se marca como inactiva.</p>
     *
     * <p>El contenido musical del artista debe gestionarse desde el microservicio de Contenidos.</p>
     *
     * @param id ID del artista
     * @param authentication Autenticación del usuario
     * @return Confirmación de la eliminación
     */
    @DeleteMapping("/artistas/{id}")
    public ResponseEntity<SuccessfulResponseDTO> eliminarArtista(
            @PathVariable Long id,
            Authentication authentication) {

        Long authenticatedUserId = Long.parseLong(authentication.getName());

        artistaService.eliminarArtista(id, authenticatedUserId);

        SuccessfulResponseDTO response = SuccessfulResponseDTO.builder()
                .successful("Eliminación de artista exitosa")
                .message("El perfil del artista ha sido eliminado correctamente")
                .statusCode(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.ok(response);
    }
}