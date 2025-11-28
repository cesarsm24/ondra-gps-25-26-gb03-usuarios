package com.ondra.users.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ondra.users.dto.RedSocialCrearDTO;
import com.ondra.users.dto.RedSocialDTO;
import com.ondra.users.dto.RedSocialEditarDTO;
import com.ondra.users.exceptions.*;
import com.ondra.users.repositories.RefreshTokenRepository;
import com.ondra.users.security.*;
import com.ondra.users.services.RedSocialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitarios para {@link RedSocialController}
 *
 * <p>
 * Estos tests cubren la funcionalidad de listar, crear, editar y eliminar
 * redes sociales asociadas a artistas, incluyendo casos de éxito, errores
 * de validación, permisos insuficientes y entidades no existentes.
 * </p>
 *
 */
@WebMvcTest(RedSocialController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, ServiceTokenFilter.class, TestJwtHelper.class})
class RedSocialControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestJwtHelper testJwtHelper;

    @MockBean
    private RedSocialService redSocialService;

    @MockBean
    private RefreshTokenRepository refreshTokenRepository;

    private RedSocialDTO redSocialDTO;
    private RedSocialDTO redSocialDTO2;

    @BeforeEach
    void setUp() {
        redSocialDTO = RedSocialDTO.builder()
                .idRedSocial(1L)
                .idArtista(1L)
                .tipoRedSocial("instagram")
                .urlRedSocial("https://instagram.com/djcarlos")
                .build();

        redSocialDTO2 = RedSocialDTO.builder()
                .idRedSocial(2L)
                .idArtista(1L)
                .tipoRedSocial("spotify")
                .urlRedSocial("https://open.spotify.com/artist/djcarlos")
                .build();
    }

    // ==================== TESTS LISTAR REDES SOCIALES ====================

    @Test
    @DisplayName("Listar redes sociales exitoso")
    void listarRedesSociales_Success() throws Exception {
        List<RedSocialDTO> redes = Arrays.asList(redSocialDTO, redSocialDTO2);

        when(redSocialService.listarRedesSociales(1L)).thenReturn(redes);

        mockMvc.perform(get("/api/artistas/1/redes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].idRedSocial").value(1))
                .andExpect(jsonPath("$[0].tipoRedSocial").value("instagram"))
                .andExpect(jsonPath("$[0].urlRedSocial").value("https://instagram.com/djcarlos"))
                .andExpect(jsonPath("$[1].idRedSocial").value(2))
                .andExpect(jsonPath("$[1].tipoRedSocial").value("spotify"));

        verify(redSocialService, times(1)).listarRedesSociales(1L);
    }

    @Test
    @DisplayName("Listar redes sociales - Artista no existe")
    void listarRedesSociales_ArtistaNoExiste() throws Exception {
        when(redSocialService.listarRedesSociales(999L))
                .thenThrow(new ArtistaNotFoundException(999L));

        mockMvc.perform(get("/api/artistas/999/redes"))
                .andExpect(status().isNotFound());

        verify(redSocialService, times(1)).listarRedesSociales(999L);
    }

    @Test
    @DisplayName("Listar redes sociales - Lista vacía")
    void listarRedesSociales_ListaVacia() throws Exception {
        when(redSocialService.listarRedesSociales(1L)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/artistas/1/redes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(redSocialService, times(1)).listarRedesSociales(1L);
    }

    // ==================== TESTS CREAR RED SOCIAL ====================

    @Test
    @DisplayName("Crear red social exitoso")
    void crearRedSocial_Success() throws Exception {
        // Usuario 1 es propietario del artista 1
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "carlos@example.com");

        RedSocialCrearDTO crearDTO = RedSocialCrearDTO.builder()
                .tipoRedSocial("instagram")
                .urlRedSocial("https://instagram.com/djcarlos")
                .build();

        when(redSocialService.crearRedSocial(eq(1L), any(RedSocialCrearDTO.class), eq(1L)))
                .thenReturn(redSocialDTO);

        mockMvc.perform(post("/api/artistas/1/redes")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idRedSocial").value(1))
                .andExpect(jsonPath("$.tipoRedSocial").value("instagram"))
                .andExpect(jsonPath("$.urlRedSocial").value("https://instagram.com/djcarlos"));

        verify(redSocialService, times(1)).crearRedSocial(eq(1L), any(RedSocialCrearDTO.class), eq(1L));
    }

    @Test
    @DisplayName("Crear red social - Artista no existe")
    void crearRedSocial_ArtistaNoExiste() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "carlos@example.com");

        RedSocialCrearDTO crearDTO = RedSocialCrearDTO.builder()
                .tipoRedSocial("instagram")
                .urlRedSocial("https://instagram.com/djcarlos")
                .build();

        when(redSocialService.crearRedSocial(eq(999L), any(RedSocialCrearDTO.class), eq(1L)))
                .thenThrow(new ArtistaNotFoundException(999L));

        mockMvc.perform(post("/api/artistas/999/redes")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearDTO)))
                .andExpect(status().isNotFound());

        verify(redSocialService, times(1)).crearRedSocial(eq(999L), any(RedSocialCrearDTO.class), eq(1L));
    }

    @Test
    @DisplayName("Crear red social - Sin permisos (otro usuario)")
    void crearRedSocial_SinPermisos() throws Exception {
        // Usuario 2 intenta crear red social para artista 1
        String token = testJwtHelper.generarTokenPrueba(2L, "otro@example.com");

        RedSocialCrearDTO crearDTO = RedSocialCrearDTO.builder()
                .tipoRedSocial("instagram")
                .urlRedSocial("https://instagram.com/djcarlos")
                .build();

        when(redSocialService.crearRedSocial(eq(1L), any(RedSocialCrearDTO.class), eq(2L)))
                .thenThrow(new ForbiddenAccessException("No tienes permiso para añadir redes sociales a este perfil"));

        mockMvc.perform(post("/api/artistas/1/redes")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearDTO)))
                .andExpect(status().isForbidden());

        verify(redSocialService, times(1)).crearRedSocial(eq(1L), any(RedSocialCrearDTO.class), eq(2L));
    }

    @Test
    @DisplayName("Crear red social - Tipo inválido (validación DTO)")
    void crearRedSocial_TipoInvalido() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "carlos@example.com");

        RedSocialCrearDTO crearDTO = RedSocialCrearDTO.builder()
                .tipoRedSocial("invalid_social")
                .urlRedSocial("https://example.com")
                .build();

        // La validación @Pattern en el DTO rechaza el valor antes de llegar al servicio
        mockMvc.perform(post("/api/artistas/1/redes")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        // El servicio no se invoca porque la validación del DTO falla antes
        verify(redSocialService, never()).crearRedSocial(anyLong(), any(RedSocialCrearDTO.class), anyLong());
    }

    @Test
    @DisplayName("Crear red social - Red social duplicada")
    void crearRedSocial_Duplicada() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "carlos@example.com");

        RedSocialCrearDTO crearDTO = RedSocialCrearDTO.builder()
                .tipoRedSocial("instagram")
                .urlRedSocial("https://instagram.com/djcarlos2")
                .build();

        when(redSocialService.crearRedSocial(eq(1L), any(RedSocialCrearDTO.class), eq(1L)))
                .thenThrow(new DuplicateSocialNetworkException("instagram"));

        mockMvc.perform(post("/api/artistas/1/redes")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearDTO)))
                .andExpect(status().isConflict());

        verify(redSocialService, times(1)).crearRedSocial(eq(1L), any(RedSocialCrearDTO.class), eq(1L));
    }

    @Test
    @DisplayName("Crear red social - Sin autenticación")
    void crearRedSocial_SinAutenticacion() throws Exception {
        RedSocialCrearDTO crearDTO = RedSocialCrearDTO.builder()
                .tipoRedSocial("instagram")
                .urlRedSocial("https://instagram.com/djcarlos")
                .build();

        mockMvc.perform(post("/api/artistas/1/redes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearDTO)))
                .andExpect(status().isForbidden());

        verify(redSocialService, never()).crearRedSocial(anyLong(), any(RedSocialCrearDTO.class), anyLong());
    }

    // ==================== TESTS EDITAR RED SOCIAL ====================

    @Test
    @DisplayName("Editar red social exitoso")
    void editarRedSocial_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "carlos@example.com");

        RedSocialEditarDTO editarDTO = RedSocialEditarDTO.builder()
                .urlRedSocial("https://instagram.com/djcarlos_updated")
                .build();

        RedSocialDTO redSocialActualizada = RedSocialDTO.builder()
                .idRedSocial(1L)
                .idArtista(1L)
                .tipoRedSocial("instagram")
                .urlRedSocial("https://instagram.com/djcarlos_updated")
                .build();

        when(redSocialService.editarRedSocial(eq(1L), eq(1L), any(RedSocialEditarDTO.class), eq(1L)))
                .thenReturn(redSocialActualizada);

        mockMvc.perform(put("/api/artistas/1/redes/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editarDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idRedSocial").value(1))
                .andExpect(jsonPath("$.urlRedSocial").value("https://instagram.com/djcarlos_updated"));

        verify(redSocialService, times(1)).editarRedSocial(eq(1L), eq(1L), any(RedSocialEditarDTO.class), eq(1L));
    }

    @Test
    @DisplayName("Editar red social - Cambiar tipo")
    void editarRedSocial_CambiarTipo() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "carlos@example.com");

        RedSocialEditarDTO editarDTO = RedSocialEditarDTO.builder()
                .tipoRedSocial("spotify")
                .urlRedSocial("https://open.spotify.com/artist/djcarlos")
                .build();

        RedSocialDTO redSocialActualizada = RedSocialDTO.builder()
                .idRedSocial(1L)
                .idArtista(1L)
                .tipoRedSocial("spotify")
                .urlRedSocial("https://open.spotify.com/artist/djcarlos")
                .build();

        when(redSocialService.editarRedSocial(eq(1L), eq(1L), any(RedSocialEditarDTO.class), eq(1L)))
                .thenReturn(redSocialActualizada);

        mockMvc.perform(put("/api/artistas/1/redes/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editarDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoRedSocial").value("spotify"))
                .andExpect(jsonPath("$.urlRedSocial").value("https://open.spotify.com/artist/djcarlos"));

        verify(redSocialService, times(1)).editarRedSocial(eq(1L), eq(1L), any(RedSocialEditarDTO.class), eq(1L));
    }

    @Test
    @DisplayName("Editar red social - Red social no existe")
    void editarRedSocial_NoExiste() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "carlos@example.com");

        RedSocialEditarDTO editarDTO = RedSocialEditarDTO.builder()
                .urlRedSocial("https://instagram.com/updated")
                .build();

        when(redSocialService.editarRedSocial(eq(1L), eq(999L), any(RedSocialEditarDTO.class), eq(1L)))
                .thenThrow(new RedSocialNotFoundException(999L));

        mockMvc.perform(put("/api/artistas/1/redes/999")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editarDTO)))
                .andExpect(status().isNotFound());

        verify(redSocialService, times(1)).editarRedSocial(eq(1L), eq(999L), any(RedSocialEditarDTO.class), eq(1L));
    }

    @Test
    @DisplayName("Editar red social - Sin permisos")
    void editarRedSocial_SinPermisos() throws Exception {
        // Usuario 2 intenta editar red social del artista 1
        String token = testJwtHelper.generarTokenPrueba(2L, "otro@example.com");

        RedSocialEditarDTO editarDTO = RedSocialEditarDTO.builder()
                .urlRedSocial("https://instagram.com/hack")
                .build();

        when(redSocialService.editarRedSocial(eq(1L), eq(1L), any(RedSocialEditarDTO.class), eq(2L)))
                .thenThrow(new ForbiddenAccessException("No tienes permiso para modificar redes sociales de este perfil"));

        mockMvc.perform(put("/api/artistas/1/redes/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editarDTO)))
                .andExpect(status().isForbidden());

        verify(redSocialService, times(1)).editarRedSocial(eq(1L), eq(1L), any(RedSocialEditarDTO.class), eq(2L));
    }

    @Test
    @DisplayName("Editar red social - Red no pertenece al artista")
    void editarRedSocial_NoPertenece() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "carlos@example.com");

        RedSocialEditarDTO editarDTO = RedSocialEditarDTO.builder()
                .urlRedSocial("https://instagram.com/updated")
                .build();

        when(redSocialService.editarRedSocial(eq(1L), eq(5L), any(RedSocialEditarDTO.class), eq(1L)))
                .thenThrow(new SocialNetworkMismatchException());

        mockMvc.perform(put("/api/artistas/1/redes/5")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editarDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("SOCIAL_NETWORK_MISMATCH"));

        verify(redSocialService, times(1)).editarRedSocial(eq(1L), eq(5L), any(RedSocialEditarDTO.class), eq(1L));
    }

    @Test
    @DisplayName("Editar red social - Tipo inválido (validación DTO)")
    void editarRedSocial_TipoInvalido() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "carlos@example.com");

        RedSocialEditarDTO editarDTO = RedSocialEditarDTO.builder()
                .tipoRedSocial("invalid_type")
                .build();

        // La validación @Pattern en el DTO rechaza el valor antes de llegar al servicio
        mockMvc.perform(put("/api/artistas/1/redes/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editarDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        // El servicio no se invoca porque la validación del DTO falla antes
        verify(redSocialService, never()).editarRedSocial(anyLong(), anyLong(), any(RedSocialEditarDTO.class), anyLong());
    }

    @Test
    @DisplayName("Editar red social - Tipo duplicado")
    void editarRedSocial_TipoDuplicado() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "carlos@example.com");

        RedSocialEditarDTO editarDTO = RedSocialEditarDTO.builder()
                .tipoRedSocial("spotify")
                .build();

        when(redSocialService.editarRedSocial(eq(1L), eq(1L), any(RedSocialEditarDTO.class), eq(1L)))
                .thenThrow(new DuplicateSocialNetworkException("spotify"));

        mockMvc.perform(put("/api/artistas/1/redes/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editarDTO)))
                .andExpect(status().isConflict());

        verify(redSocialService, times(1)).editarRedSocial(eq(1L), eq(1L), any(RedSocialEditarDTO.class), eq(1L));
    }

    // ==================== TESTS ELIMINAR RED SOCIAL ====================

    @Test
    @DisplayName("Eliminar red social exitoso")
    void eliminarRedSocial_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "carlos@example.com");

        doNothing().when(redSocialService).eliminarRedSocial(1L, 1L, 1L);

        mockMvc.perform(delete("/api/artistas/1/redes/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successful").value("Eliminación de red social exitosa"))
                .andExpect(jsonPath("$.message").value("La red social ha sido eliminada correctamente"))
                .andExpect(jsonPath("$.statusCode").value(200));

        verify(redSocialService, times(1)).eliminarRedSocial(1L, 1L, 1L);
    }

    @Test
    @DisplayName("Eliminar red social - Red no existe")
    void eliminarRedSocial_NoExiste() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "carlos@example.com");

        doThrow(new RedSocialNotFoundException(999L))
                .when(redSocialService).eliminarRedSocial(1L, 999L, 1L);

        mockMvc.perform(delete("/api/artistas/1/redes/999")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        verify(redSocialService, times(1)).eliminarRedSocial(1L, 999L, 1L);
    }

    @Test
    @DisplayName("Eliminar red social - Sin permisos")
    void eliminarRedSocial_SinPermisos() throws Exception {
        // Usuario 2 intenta eliminar red social del artista 1
        String token = testJwtHelper.generarTokenPrueba(2L, "otro@example.com");

        doThrow(new ForbiddenAccessException("No tienes permiso para eliminar redes sociales de este perfil"))
                .when(redSocialService).eliminarRedSocial(1L, 1L, 2L);

        mockMvc.perform(delete("/api/artistas/1/redes/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        verify(redSocialService, times(1)).eliminarRedSocial(1L, 1L, 2L);
    }

    @Test
    @DisplayName("Eliminar red social - Artista no existe")
    void eliminarRedSocial_ArtistaNoExiste() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "carlos@example.com");

        doThrow(new ArtistaNotFoundException(999L))
                .when(redSocialService).eliminarRedSocial(999L, 1L, 1L);

        mockMvc.perform(delete("/api/artistas/999/redes/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        verify(redSocialService, times(1)).eliminarRedSocial(999L, 1L, 1L);
    }

    @Test
    @DisplayName("Eliminar red social - Red no pertenece al artista")
    void eliminarRedSocial_NoPertenece() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "carlos@example.com");

        doThrow(new SocialNetworkMismatchException())
                .when(redSocialService).eliminarRedSocial(1L, 5L, 1L);

        mockMvc.perform(delete("/api/artistas/1/redes/5")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("SOCIAL_NETWORK_MISMATCH"));

        verify(redSocialService, times(1)).eliminarRedSocial(1L, 5L, 1L);
    }

    @Test
    @DisplayName("Eliminar red social - Sin autenticación")
    void eliminarRedSocial_SinAutenticacion() throws Exception {
        mockMvc.perform(delete("/api/artistas/1/redes/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(redSocialService, never()).eliminarRedSocial(anyLong(), anyLong(), anyLong());
    }
}