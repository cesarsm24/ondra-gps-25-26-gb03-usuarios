package com.ondra.users.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ondra.users.dto.*;
import com.ondra.users.exceptions.*;
import com.ondra.users.repositories.RefreshTokenRepository;
import com.ondra.users.security.*;
import com.ondra.users.services.ArtistaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitarios para ArtistaController con MockMvc y tokens JWT reales.
 */
@WebMvcTest(ArtistaController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, ServiceTokenFilter.class, TestJwtHelper.class})
class ArtistaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestJwtHelper testJwtHelper;

    @MockBean
    private ArtistaService artistaService;

    @MockBean
    private RefreshTokenRepository refreshTokenRepository;

    private ArtistaDTO artistaDTO;
    private List<ArtistaDTO> listaArtistas;

    @BeforeEach
    void setUp() {
        artistaDTO = ArtistaDTO.builder()
                .idArtista(1L)
                .idUsuario(10L)
                .nombreArtistico("El Gran Artista")
                .biografiaArtistico("Artista reconocido mundialmente")
                .fotoPerfilArtistico("http://example.com/artista.jpg")
                .esTendencia(true)
                .build();

        ArtistaDTO artista2 = ArtistaDTO.builder()
                .idArtista(2L)
                .idUsuario(20L)
                .nombreArtistico("La Estrella")
                .biografiaArtistico("Nueva promesa de la música")
                .esTendencia(false)
                .build();

        listaArtistas = Arrays.asList(artistaDTO, artista2);
    }

    // ==================== TESTS LISTAR ARTISTAS TENDENCIA ====================

    @Test
    @DisplayName("Listar artistas en tendencia - exitoso")
    void listarArtistasTendencia_Success() throws Exception {
        when(artistaService.listarArtistasTendencia(5)).thenReturn(listaArtistas);

        mockMvc.perform(get("/api/artistas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombreArtistico").value("El Gran Artista"))
                .andExpect(jsonPath("$[0].esTendencia").value(true));

        verify(artistaService, times(1)).listarArtistasTendencia(5);
    }

    // ==================== TESTS BUSCAR ARTISTAS ====================

    @Test
    @DisplayName("Buscar artistas sin filtros")
    void buscarArtistas_SinFiltros_Success() throws Exception {
        Page<ArtistaDTO> page = new PageImpl<>(listaArtistas);
        when(artistaService.buscarArtistas(null, null, "most_recent", 0, 20))
                .thenReturn(page);

        mockMvc.perform(get("/api/artistas/buscar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));

        verify(artistaService, times(1)).buscarArtistas(null, null, "most_recent", 0, 20);
    }

    @Test
    @DisplayName("Buscar artistas con filtros combinados")
    void buscarArtistas_FiltrosCombinados() throws Exception {
        Page<ArtistaDTO> page = new PageImpl<>(Collections.singletonList(artistaDTO));
        when(artistaService.buscarArtistas("Gran", true, "oldest", 0, 10))
                .thenReturn(page);

        mockMvc.perform(get("/api/artistas/buscar")
                        .param("search", "Gran")
                        .param("esTendencia", "true")
                        .param("orderBy", "oldest")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        verify(artistaService, times(1)).buscarArtistas("Gran", true, "oldest", 0, 10);
    }

    // ==================== TESTS CONVERTIRSE EN ARTISTA ====================

    @Test
    @DisplayName("Convertirse en artista - exitoso sin foto")
    void convertirseEnArtista_SinFoto_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(10L, "usuario@example.com");

        CrearArtistaDTO crearDTO = CrearArtistaDTO.builder()
                .nombreArtistico("Nuevo Artista")
                .biografiaArtistico("Mi biografía artística")
                .build();

        when(artistaService.convertirseEnArtista(any(CrearArtistaDTO.class), isNull(), eq(10L)))
                .thenReturn(artistaDTO);

        MockMultipartFile datosFile = new MockMultipartFile(
                "datos",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(crearDTO)
        );

        mockMvc.perform(multipart("/api/convertirse-artista")
                        .file(datosFile)
                        .header("Authorization", "Bearer " + token)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreArtistico").value("El Gran Artista"));

        verify(artistaService, times(1))
                .convertirseEnArtista(any(CrearArtistaDTO.class), isNull(), eq(10L));
    }

    @Test
    @DisplayName("Convertirse en artista - usuario ya es artista")
    void convertirseEnArtista_YaEsArtista() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(10L, "usuario@example.com");

        CrearArtistaDTO crearDTO = CrearArtistaDTO.builder()
                .nombreArtistico("Otro Nombre")
                .biografiaArtistico("Otra bio")
                .build();

        MockMultipartFile datosFile = new MockMultipartFile(
                "datos",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(crearDTO)
        );

        when(artistaService.convertirseEnArtista(any(CrearArtistaDTO.class), isNull(), eq(10L)))
                .thenThrow(new InvalidDataException("Este usuario ya es un artista"));

        mockMvc.perform(multipart("/api/convertirse-artista")
                        .file(datosFile)
                        .header("Authorization", "Bearer " + token)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(artistaService, times(1))
                .convertirseEnArtista(any(CrearArtistaDTO.class), isNull(), eq(10L));
    }

    // ==================== TESTS RENUNCIAR PERFIL ARTISTA ====================

    @Test
    @DisplayName("Renunciar a perfil artista - exitoso")
    void renunciarPerfilArtista_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(10L, 1L, "artista@example.com");

        doNothing().when(artistaService).renunciarPerfilArtista(1L, 10L);

        mockMvc.perform(post("/api/artistas/1/renunciar")
                        .header("Authorization", "Bearer " + token)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successful").value("Renuncia de perfil artístico exitosa"));

        verify(artistaService, times(1)).renunciarPerfilArtista(1L, 10L);
    }

    @Test
    @DisplayName("Renunciar a perfil de otro artista - Forbidden")
    void renunciarPerfilArtista_OtroArtista_Forbidden() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(20L, 2L, "otro@example.com");

        doThrow(new ForbiddenAccessException("No tienes permiso para renunciar a este perfil de artista"))
                .when(artistaService).renunciarPerfilArtista(1L, 20L);

        mockMvc.perform(post("/api/artistas/1/renunciar")
                        .header("Authorization", "Bearer " + token)
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(artistaService, times(1)).renunciarPerfilArtista(1L, 20L);
    }

    // ==================== TESTS OBTENER ARTISTA ====================

    @Test
    @DisplayName("Obtener artista por ID - exitoso")
    void obtenerArtista_Success() throws Exception {
        when(artistaService.obtenerArtista(1L)).thenReturn(artistaDTO);

        mockMvc.perform(get("/api/artistas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idArtista").value(1))
                .andExpect(jsonPath("$.nombreArtistico").value("El Gran Artista"));

        verify(artistaService, times(1)).obtenerArtista(1L);
    }

    @Test
    @DisplayName("Obtener artista inexistente - Not Found")
    void obtenerArtista_NoExiste() throws Exception {
        when(artistaService.obtenerArtista(999L))
                .thenThrow(new ArtistaNotFoundException(999L));

        mockMvc.perform(get("/api/artistas/999"))
                .andExpect(status().isNotFound());

        verify(artistaService, times(1)).obtenerArtista(999L);
    }

    // ==================== TESTS EDITAR ARTISTA ====================

    @Test
    @DisplayName("Editar artista - exitoso")
    void editarArtista_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(10L, 1L, "artista@example.com");

        EditarArtistaDTO editarDTO = EditarArtistaDTO.builder()
                .nombreArtistico("Nombre Actualizado")
                .biografiaArtistico("Biografía actualizada")
                .build();

        ArtistaDTO artistaActualizado = ArtistaDTO.builder()
                .idArtista(1L)
                .idUsuario(10L)
                .nombreArtistico("Nombre Actualizado")
                .biografiaArtistico("Biografía actualizada")
                .build();

        when(artistaService.editarArtista(eq(1L), any(EditarArtistaDTO.class), eq(10L)))
                .thenReturn(artistaActualizado);

        mockMvc.perform(put("/api/artistas/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editarDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreArtistico").value("Nombre Actualizado"));

        verify(artistaService, times(1))
                .editarArtista(eq(1L), any(EditarArtistaDTO.class), eq(10L));
    }

    @Test
    @DisplayName("Editar artista de otro usuario - Forbidden")
    void editarArtista_OtroUsuario_Forbidden() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(20L, 2L, "otro@example.com");

        EditarArtistaDTO editarDTO = EditarArtistaDTO.builder()
                .nombreArtistico("Intento Hackeo")
                .build();

        when(artistaService.editarArtista(eq(1L), any(EditarArtistaDTO.class), eq(20L)))
                .thenThrow(new ForbiddenAccessException("No tienes permiso para modificar este perfil de artista"));

        mockMvc.perform(put("/api/artistas/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editarDTO)))
                .andExpect(status().isForbidden());

        verify(artistaService, times(1))
                .editarArtista(eq(1L), any(EditarArtistaDTO.class), eq(20L));
    }

    // ==================== TESTS ELIMINAR ARTISTA ====================

    @Test
    @DisplayName("Eliminar artista - exitoso")
    void eliminarArtista_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(10L, 1L, "artista@example.com");

        doNothing().when(artistaService).eliminarArtista(1L, 10L);

        mockMvc.perform(delete("/api/artistas/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successful").value("Eliminación realizada"));

        verify(artistaService, times(1)).eliminarArtista(1L, 10L);
    }

    @Test
    @DisplayName("Eliminar artista de otro usuario - Forbidden")
    void eliminarArtista_OtroUsuario_Forbidden() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(20L, 2L, "otro@example.com");

        doThrow(new ForbiddenAccessException("No tienes permiso para eliminar este perfil de artista"))
                .when(artistaService).eliminarArtista(1L, 20L);

        mockMvc.perform(delete("/api/artistas/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        verify(artistaService, times(1)).eliminarArtista(1L, 20L);
    }
}