package com.ondra.users.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ondra.users.dto.*;
import com.ondra.users.exceptions.*;
import com.ondra.users.models.enums.TipoUsuario;
import com.ondra.users.repositories.RefreshTokenRepository;
import com.ondra.users.security.*;
import com.ondra.users.services.SeguimientoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
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
 * Tests unitarios para {@link SeguimientoController}
 *
 * <p>
 * Estos tests cubren la funcionalidad de seguir y dejar de seguir usuarios,
 * así como la obtención de listas de seguidores/seguidos, verificación de seguimiento
 * y estadísticas de seguimiento. Se incluyen casos de éxito, errores de validación,
 * permisos insuficientes y entidades no existentes.
 * </p>
 *
 */
@WebMvcTest(SeguimientoController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, ServiceTokenFilter.class, TestJwtHelper.class})
class SeguimientoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestJwtHelper testJwtHelper;

    @MockBean
    private SeguimientoService seguimientoService;

    @MockBean
    private RefreshTokenRepository refreshTokenRepository;

    private SeguimientoDTO seguimientoDTO;
    private UsuarioBasicoDTO usuarioBasicoSeguidor;
    private UsuarioBasicoDTO usuarioBasicoSeguido;
    private UsuarioBasicoDTO artistaBasico;

    @BeforeEach
    void setUp() {
        usuarioBasicoSeguidor = UsuarioBasicoDTO.builder()
                .idUsuario(1L)
                .nombreUsuario("Juan")
                .apellidosUsuario("Pérez")
                .fotoPerfil("http://example.com/foto1.jpg")
                .tipoUsuario(TipoUsuario.NORMAL)
                .slug("juan-perez")
                .build();

        usuarioBasicoSeguido = UsuarioBasicoDTO.builder()
                .idUsuario(2L)
                .nombreUsuario("María")
                .apellidosUsuario("García")
                .fotoPerfil("http://example.com/foto2.jpg")
                .tipoUsuario(TipoUsuario.NORMAL)
                .slug("maria-garcia")
                .build();

        artistaBasico = UsuarioBasicoDTO.builder()
                .idUsuario(3L)
                .nombreUsuario("Carlos")
                .apellidosUsuario("López")
                .fotoPerfil("http://example.com/foto3.jpg")
                .tipoUsuario(TipoUsuario.ARTISTA)
                .nombreArtistico("DJ Carlos")
                .slug("carlos-lopez")
                .slugArtistico("dj-carlos")
                .build();

        seguimientoDTO = SeguimientoDTO.builder()
                .idSeguimiento(1L)
                .seguidor(usuarioBasicoSeguidor)
                .seguido(usuarioBasicoSeguido)
                .fechaSeguimiento(LocalDateTime.now())
                .build();
    }

    // ==================== TESTS SEGUIR USUARIO ====================

    @Test
    @DisplayName("Seguir usuario exitoso")
    void seguirUsuario_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "juan@example.com");

        SeguirUsuarioDTO seguirDTO = SeguirUsuarioDTO.builder()
                .idUsuarioASeguir(2L)
                .build();

        when(seguimientoService.seguirUsuario(eq(1L), eq(2L), eq(1L)))
                .thenReturn(seguimientoDTO);

        mockMvc.perform(post("/api/seguimientos")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(seguirDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idSeguimiento").value(1))
                .andExpect(jsonPath("$.seguidor.idUsuario").value(1))
                .andExpect(jsonPath("$.seguido.idUsuario").value(2))
                .andExpect(jsonPath("$.fechaSeguimiento").exists());

        verify(seguimientoService, times(1)).seguirUsuario(eq(1L), eq(2L), eq(1L));
    }

    @Test
    @DisplayName("Seguir usuario - Intento de seguirse a sí mismo")
    void seguirUsuario_MismoUsuario_BadRequest() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "juan@example.com");

        SeguirUsuarioDTO seguirDTO = SeguirUsuarioDTO.builder()
                .idUsuarioASeguir(1L)
                .build();

        when(seguimientoService.seguirUsuario(eq(1L), eq(1L), eq(1L)))
                .thenThrow(new InvalidFollowException("No puedes seguirte a ti mismo"));

        mockMvc.perform(post("/api/seguimientos")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(seguirDTO)))
                .andExpect(status().isBadRequest());

        verify(seguimientoService, times(1)).seguirUsuario(eq(1L), eq(1L), eq(1L));
    }

    @Test
    @DisplayName("Seguir usuario - Usuario a seguir no existe")
    void seguirUsuario_UsuarioNoExiste() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "juan@example.com");

        SeguirUsuarioDTO seguirDTO = SeguirUsuarioDTO.builder()
                .idUsuarioASeguir(999L)
                .build();

        when(seguimientoService.seguirUsuario(eq(1L), eq(999L), eq(1L)))
                .thenThrow(new UsuarioNotFoundException(999L));

        mockMvc.perform(post("/api/seguimientos")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(seguirDTO)))
                .andExpect(status().isNotFound());

        verify(seguimientoService, times(1)).seguirUsuario(eq(1L), eq(999L), eq(1L));
    }

    @Test
    @DisplayName("Seguir usuario - Seguimiento duplicado")
    void seguirUsuario_SeguimientoDuplicado() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "juan@example.com");

        SeguirUsuarioDTO seguirDTO = SeguirUsuarioDTO.builder()
                .idUsuarioASeguir(2L)
                .build();

        when(seguimientoService.seguirUsuario(eq(1L), eq(2L), eq(1L)))
                .thenThrow(new DuplicateFollowException("Ya sigues a este usuario"));

        mockMvc.perform(post("/api/seguimientos")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(seguirDTO)))
                .andExpect(status().isConflict());

        verify(seguimientoService, times(1)).seguirUsuario(eq(1L), eq(2L), eq(1L));
    }

    @Test
    @DisplayName("Seguir usuario - Artista intenta seguir")
    void seguirUsuario_ArtistaIntentaSeguir() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(3L, 1L, "carlos@example.com");

        SeguirUsuarioDTO seguirDTO = SeguirUsuarioDTO.builder()
                .idUsuarioASeguir(2L)
                .build();

        when(seguimientoService.seguirUsuario(eq(3L), eq(2L), eq(3L)))
                .thenThrow(new InvalidFollowException("Los artistas no pueden seguir a otros usuarios"));

        mockMvc.perform(post("/api/seguimientos")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(seguirDTO)))
                .andExpect(status().isBadRequest());

        verify(seguimientoService, times(1)).seguirUsuario(eq(3L), eq(2L), eq(3L));
    }

    @Test
    @DisplayName("Seguir usuario - Usuario a seguir está inactivo")
    void seguirUsuario_UsuarioInactivo() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "juan@example.com");

        SeguirUsuarioDTO seguirDTO = SeguirUsuarioDTO.builder()
                .idUsuarioASeguir(2L)
                .build();

        when(seguimientoService.seguirUsuario(eq(1L), eq(2L), eq(1L)))
                .thenThrow(new AccountInactiveException("No puedes seguir a este usuario"));

        mockMvc.perform(post("/api/seguimientos")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(seguirDTO)))
                .andExpect(status().isForbidden());

        verify(seguimientoService, times(1)).seguirUsuario(eq(1L), eq(2L), eq(1L));
    }

    @Test
    @DisplayName("Seguir usuario - Sin autenticación")
    void seguirUsuario_SinAutenticacion() throws Exception {
        SeguirUsuarioDTO seguirDTO = SeguirUsuarioDTO.builder()
                .idUsuarioASeguir(2L)
                .build();

        mockMvc.perform(post("/api/seguimientos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(seguirDTO)))
                .andExpect(status().isForbidden());

        verify(seguimientoService, never()).seguirUsuario(anyLong(), anyLong(), anyLong());
    }

    // ==================== TESTS DEJAR DE SEGUIR ====================

    @Test
    @DisplayName("Dejar de seguir usuario exitoso")
    void dejarDeSeguir_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "juan@example.com");

        doNothing().when(seguimientoService).dejarDeSeguir(1L, 2L, 1L);

        mockMvc.perform(delete("/api/seguimientos/2")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        verify(seguimientoService, times(1)).dejarDeSeguir(1L, 2L, 1L);
    }

    @Test
    @DisplayName("Dejar de seguir - No existe el seguimiento")
    void dejarDeSeguir_NoExiste() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "juan@example.com");

        doThrow(new FollowNotFoundException("No sigues a este usuario"))
                .when(seguimientoService).dejarDeSeguir(1L, 2L, 1L);

        mockMvc.perform(delete("/api/seguimientos/2")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        verify(seguimientoService, times(1)).dejarDeSeguir(1L, 2L, 1L);
    }

    @Test
    @DisplayName("Dejar de seguir - Sin autenticación")
    void dejarDeSeguir_SinAutenticacion() throws Exception {
        mockMvc.perform(delete("/api/seguimientos/2")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(seguimientoService, never()).dejarDeSeguir(anyLong(), anyLong(), anyLong());
    }

    // ==================== TESTS OBTENER SEGUIDOS ====================

    @Test
    @DisplayName("Obtener lista de seguidos exitoso")
    void obtenerSeguidos_Success() throws Exception {
        List<UsuarioBasicoDTO> seguidos = Arrays.asList(usuarioBasicoSeguido, artistaBasico);

        when(seguimientoService.obtenerSeguidos(1L)).thenReturn(seguidos);

        mockMvc.perform(get("/api/seguimientos/1/seguidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].idUsuario").value(2))
                .andExpect(jsonPath("$[0].nombreUsuario").value("María"))
                .andExpect(jsonPath("$[1].idUsuario").value(3))
                .andExpect(jsonPath("$[1].tipoUsuario").value("ARTISTA"))
                .andExpect(jsonPath("$[1].nombreArtistico").value("DJ Carlos"));

        verify(seguimientoService, times(1)).obtenerSeguidos(1L);
    }

    @Test
    @DisplayName("Obtener seguidos - Usuario no existe")
    void obtenerSeguidos_UsuarioNoExiste() throws Exception {
        when(seguimientoService.obtenerSeguidos(999L))
                .thenThrow(new UsuarioNotFoundException(999L));

        mockMvc.perform(get("/api/seguimientos/999/seguidos"))
                .andExpect(status().isNotFound());

        verify(seguimientoService, times(1)).obtenerSeguidos(999L);
    }

    @Test
    @DisplayName("Obtener seguidos - Lista vacía")
    void obtenerSeguidos_ListaVacia() throws Exception {
        when(seguimientoService.obtenerSeguidos(1L)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/seguimientos/1/seguidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(seguimientoService, times(1)).obtenerSeguidos(1L);
    }

    // ==================== TESTS OBTENER SEGUIDORES ====================

    @Test
    @DisplayName("Obtener lista de seguidores exitoso")
    void obtenerSeguidores_Success() throws Exception {
        List<UsuarioBasicoDTO> seguidores = Arrays.asList(usuarioBasicoSeguidor);

        when(seguimientoService.obtenerSeguidores(2L)).thenReturn(seguidores);

        mockMvc.perform(get("/api/seguimientos/2/seguidores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idUsuario").value(1))
                .andExpect(jsonPath("$[0].nombreUsuario").value("Juan"));

        verify(seguimientoService, times(1)).obtenerSeguidores(2L);
    }

    @Test
    @DisplayName("Obtener seguidores - Usuario no existe")
    void obtenerSeguidores_UsuarioNoExiste() throws Exception {
        when(seguimientoService.obtenerSeguidores(999L))
                .thenThrow(new UsuarioNotFoundException(999L));

        mockMvc.perform(get("/api/seguimientos/999/seguidores"))
                .andExpect(status().isNotFound());

        verify(seguimientoService, times(1)).obtenerSeguidores(999L);
    }

    @Test
    @DisplayName("Obtener seguidores - Lista vacía")
    void obtenerSeguidores_ListaVacia() throws Exception {
        when(seguimientoService.obtenerSeguidores(2L)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/seguimientos/2/seguidores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(seguimientoService, times(1)).obtenerSeguidores(2L);
    }

    // ==================== TESTS OBTENER ESTADÍSTICAS ====================

    @Test
    @DisplayName("Obtener estadísticas de seguimiento exitoso")
    void obtenerEstadisticas_Success() throws Exception {
        EstadisticasSeguimientoDTO estadisticas = EstadisticasSeguimientoDTO.builder()
                .idUsuario(1L)
                .seguidos(10L)
                .seguidores(25L)
                .build();

        when(seguimientoService.obtenerEstadisticas(1L)).thenReturn(estadisticas);

        mockMvc.perform(get("/api/seguimientos/1/estadisticas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(1))
                .andExpect(jsonPath("$.seguidos").value(10))
                .andExpect(jsonPath("$.seguidores").value(25));

        verify(seguimientoService, times(1)).obtenerEstadisticas(1L);
    }

    @Test
    @DisplayName("Obtener estadísticas - Usuario no existe")
    void obtenerEstadisticas_UsuarioNoExiste() throws Exception {
        when(seguimientoService.obtenerEstadisticas(999L))
                .thenThrow(new UsuarioNotFoundException(999L));

        mockMvc.perform(get("/api/seguimientos/999/estadisticas"))
                .andExpect(status().isNotFound());

        verify(seguimientoService, times(1)).obtenerEstadisticas(999L);
    }

    @Test
    @DisplayName("Obtener estadísticas - Usuario sin seguimientos")
    void obtenerEstadisticas_SinSeguimientos() throws Exception {
        EstadisticasSeguimientoDTO estadisticas = EstadisticasSeguimientoDTO.builder()
                .idUsuario(1L)
                .seguidos(0L)
                .seguidores(0L)
                .build();

        when(seguimientoService.obtenerEstadisticas(1L)).thenReturn(estadisticas);

        mockMvc.perform(get("/api/seguimientos/1/estadisticas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seguidos").value(0))
                .andExpect(jsonPath("$.seguidores").value(0));

        verify(seguimientoService, times(1)).obtenerEstadisticas(1L);
    }

    // ==================== TESTS VERIFICAR SEGUIMIENTO ====================

    @Test
    @DisplayName("Verificar seguimiento - Existe")
    void verificarSeguimiento_Existe() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "juan@example.com");

        when(seguimientoService.verificarSeguimiento(1L, 2L)).thenReturn(true);

        mockMvc.perform(get("/api/seguimientos/2/verificar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(seguimientoService, times(1)).verificarSeguimiento(1L, 2L);
    }

    @Test
    @DisplayName("Verificar seguimiento - No existe")
    void verificarSeguimiento_NoExiste() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "juan@example.com");

        when(seguimientoService.verificarSeguimiento(1L, 3L)).thenReturn(false);

        mockMvc.perform(get("/api/seguimientos/3/verificar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(seguimientoService, times(1)).verificarSeguimiento(1L, 3L);
    }

    @Test
    @DisplayName("Verificar seguimiento - Sin autenticación")
    void verificarSeguimiento_SinAutenticacion() throws Exception {
        mockMvc.perform(get("/api/seguimientos/2/verificar"))
                .andExpect(status().isForbidden());

        verify(seguimientoService, never()).verificarSeguimiento(anyLong(), anyLong());
    }
}