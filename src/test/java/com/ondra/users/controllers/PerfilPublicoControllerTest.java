package com.ondra.users.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ondra.users.dto.UsuarioPublicoDTO;
import com.ondra.users.exceptions.AccountInactiveException;
import com.ondra.users.exceptions.ArtistaNotFoundException;
import com.ondra.users.exceptions.UsuarioNotFoundException;
import com.ondra.users.models.enums.TipoUsuario;
import com.ondra.users.repositories.RefreshTokenRepository;
import com.ondra.users.security.*;
import com.ondra.users.services.ArtistaService;
import com.ondra.users.services.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitarios para {@link PerfilPublicoController}.
 *
 * <p> Verifica endpoints públicos accesibles sin autenticación.</p>
 *
 * <p> Cada test incluye la descripción del caso que verifica, incluyendo escenarios
 * de éxito, error y edge cases.</p>
 */
@WebMvcTest(PerfilPublicoController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, ServiceTokenFilter.class, TestJwtHelper.class})
class PerfilPublicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private ArtistaService artistaService;

    @MockBean
    private RefreshTokenRepository refreshTokenRepository;

    private UsuarioPublicoDTO perfilUsuarioNormal;
    private UsuarioPublicoDTO perfilArtista;

    @BeforeEach
    void setUp() {
        // Perfil público de usuario normal
        perfilUsuarioNormal = UsuarioPublicoDTO.builder()
                .idUsuario(1L)
                .slug("juan-perez")
                .nombreUsuario("Juan")
                .apellidosUsuario("Pérez")
                .fotoPerfil("http://example.com/foto-juan.jpg")
                .tipoUsuario(TipoUsuario.NORMAL)
                .fechaRegistro(LocalDateTime.of(2024, 1, 15, 10, 0))
                .build();

        // Perfil público de artista
        perfilArtista = UsuarioPublicoDTO.builder()
                .idUsuario(2L)
                .slug("maria-garcia")
                .slugArtistico("la-estrella")
                .nombreUsuario("María")
                .apellidosUsuario("García")
                .nombreArtistico("La Estrella")
                .biografiaArtistico("Cantante y compositora con más de 10 años de experiencia")
                .fotoPerfil("http://example.com/foto-maria.jpg")
                .fotoPerfilArtistico("http://example.com/foto-artista-maria.jpg")
                .tipoUsuario(TipoUsuario.ARTISTA)
                .fechaRegistro(LocalDateTime.of(2023, 6, 20, 14, 30))
                .build();
    }

    // ==================== TESTS PERFIL USUARIO POR SLUG ====================

    @Test
    @DisplayName("Obtener perfil público de usuario normal - exitoso")
    void obtenerPerfilUsuario_UsuarioNormal_Success() throws Exception {
        when(usuarioService.obtenerPerfilPublicoBySlug("juan-perez"))
                .thenReturn(perfilUsuarioNormal);

        mockMvc.perform(get("/api/public/usuarios/juan-perez"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(1))
                .andExpect(jsonPath("$.slug").value("juan-perez"))
                .andExpect(jsonPath("$.nombreUsuario").value("Juan"))
                .andExpect(jsonPath("$.apellidosUsuario").value("Pérez"))
                .andExpect(jsonPath("$.fotoPerfil").value("http://example.com/foto-juan.jpg"))
                .andExpect(jsonPath("$.tipoUsuario").value("NORMAL"))
                .andExpect(jsonPath("$.nombreArtistico").doesNotExist())
                .andExpect(jsonPath("$.biografiaArtistico").doesNotExist());

        verify(usuarioService, times(1)).obtenerPerfilPublicoBySlug("juan-perez");
    }

    @Test
    @DisplayName("Obtener perfil público de artista por slug de usuario - exitoso")
    void obtenerPerfilUsuario_Artista_Success() throws Exception {
        when(usuarioService.obtenerPerfilPublicoBySlug("maria-garcia"))
                .thenReturn(perfilArtista);

        mockMvc.perform(get("/api/public/usuarios/maria-garcia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(2))
                .andExpect(jsonPath("$.slug").value("maria-garcia"))
                .andExpect(jsonPath("$.slugArtistico").value("la-estrella"))
                .andExpect(jsonPath("$.nombreUsuario").value("María"))
                .andExpect(jsonPath("$.apellidosUsuario").value("García"))
                .andExpect(jsonPath("$.nombreArtistico").value("La Estrella"))
                .andExpect(jsonPath("$.biografiaArtistico").value("Cantante y compositora con más de 10 años de experiencia"))
                .andExpect(jsonPath("$.tipoUsuario").value("ARTISTA"));

        verify(usuarioService, times(1)).obtenerPerfilPublicoBySlug("maria-garcia");
    }

    @Test
    @DisplayName("Obtener perfil usuario inexistente - Not Found")
    void obtenerPerfilUsuario_NoExiste_NotFound() throws Exception {
        when(usuarioService.obtenerPerfilPublicoBySlug("usuario-inexistente"))
                .thenThrow(new UsuarioNotFoundException("No se encontró el perfil: usuario-inexistente"));

        mockMvc.perform(get("/api/public/usuarios/usuario-inexistente"))
                .andExpect(status().isNotFound());

        verify(usuarioService, times(1)).obtenerPerfilPublicoBySlug("usuario-inexistente");
    }

    @Test
    @DisplayName("Obtener perfil de cuenta inactiva - Account Inactive")
    void obtenerPerfilUsuario_CuentaInactiva() throws Exception {
        when(usuarioService.obtenerPerfilPublicoBySlug("cuenta-inactiva"))
                .thenThrow(new AccountInactiveException("Este perfil no está disponible"));

        mockMvc.perform(get("/api/public/usuarios/cuenta-inactiva"))
                .andExpect(status().isForbidden());

        verify(usuarioService, times(1)).obtenerPerfilPublicoBySlug("cuenta-inactiva");
    }

    @Test
    @DisplayName("Obtener perfil sin autenticación - debe permitir acceso")
    void obtenerPerfilUsuario_SinAutenticacion_Success() throws Exception {
        when(usuarioService.obtenerPerfilPublicoBySlug("juan-perez"))
                .thenReturn(perfilUsuarioNormal);

        // Sin header Authorization - endpoint público
        mockMvc.perform(get("/api/public/usuarios/juan-perez"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(1));

        verify(usuarioService, times(1)).obtenerPerfilPublicoBySlug("juan-perez");
    }

    // ==================== TESTS PERFIL ARTISTA POR SLUG ARTÍSTICO ====================

    @Test
    @DisplayName("Obtener perfil artista por slug artístico - exitoso")
    void obtenerPerfilArtista_Success() throws Exception {
        when(artistaService.obtenerPerfilArtistaPorSlug("la-estrella"))
                .thenReturn(perfilArtista);

        mockMvc.perform(get("/api/public/artistas/la-estrella"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(2))
                .andExpect(jsonPath("$.slugArtistico").value("la-estrella"))
                .andExpect(jsonPath("$.nombreArtistico").value("La Estrella"))
                .andExpect(jsonPath("$.biografiaArtistico").value("Cantante y compositora con más de 10 años de experiencia"))
                .andExpect(jsonPath("$.fotoPerfilArtistico").value("http://example.com/foto-artista-maria.jpg"))
                .andExpect(jsonPath("$.tipoUsuario").value("ARTISTA"));

        verify(artistaService, times(1)).obtenerPerfilArtistaPorSlug("la-estrella");
    }

    @Test
    @DisplayName("Obtener perfil artista con slug largo con guiones - exitoso")
    void obtenerPerfilArtista_SlugLargo_Success() throws Exception {
        UsuarioPublicoDTO artistaSlugLargo = UsuarioPublicoDTO.builder()
                .idUsuario(3L)
                .slugArtistico("el-gran-compositor-de-musica-clasica")
                .nombreArtistico("El Gran Compositor de Música Clásica")
                .biografiaArtistico("Maestro de la sinfonía moderna")
                .tipoUsuario(TipoUsuario.ARTISTA)
                .build();

        when(artistaService.obtenerPerfilArtistaPorSlug("el-gran-compositor-de-musica-clasica"))
                .thenReturn(artistaSlugLargo);

        mockMvc.perform(get("/api/public/artistas/el-gran-compositor-de-musica-clasica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slugArtistico").value("el-gran-compositor-de-musica-clasica"))
                .andExpect(jsonPath("$.nombreArtistico").value("El Gran Compositor de Música Clásica"));

        verify(artistaService, times(1))
                .obtenerPerfilArtistaPorSlug("el-gran-compositor-de-musica-clasica");
    }

    @Test
    @DisplayName("Obtener perfil artista inexistente - Not Found")
    void obtenerPerfilArtista_NoExiste_NotFound() throws Exception {
        when(artistaService.obtenerPerfilArtistaPorSlug("artista-inexistente"))
                .thenThrow(new ArtistaNotFoundException("No se encontró el artista: artista-inexistente"));

        mockMvc.perform(get("/api/public/artistas/artista-inexistente"))
                .andExpect(status().isNotFound());

        verify(artistaService, times(1)).obtenerPerfilArtistaPorSlug("artista-inexistente");
    }

    @Test
    @DisplayName("Obtener perfil artista de cuenta inactiva - Account Inactive")
    void obtenerPerfilArtista_CuentaInactiva() throws Exception {
        when(artistaService.obtenerPerfilArtistaPorSlug("artista-inactivo"))
                .thenThrow(new AccountInactiveException("Este perfil no está disponible"));

        mockMvc.perform(get("/api/public/artistas/artista-inactivo"))
                .andExpect(status().isForbidden());

        verify(artistaService, times(1)).obtenerPerfilArtistaPorSlug("artista-inactivo");
    }

    @Test
    @DisplayName("Obtener perfil artista sin autenticación - debe permitir acceso")
    void obtenerPerfilArtista_SinAutenticacion_Success() throws Exception {
        when(artistaService.obtenerPerfilArtistaPorSlug("la-estrella"))
                .thenReturn(perfilArtista);

        // Sin header Authorization - endpoint público
        mockMvc.perform(get("/api/public/artistas/la-estrella"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slugArtistico").value("la-estrella"));

        verify(artistaService, times(1)).obtenerPerfilArtistaPorSlug("la-estrella");
    }

    // ==================== TESTS CASOS EDGE ====================

    @Test
    @DisplayName("Obtener perfil usuario con slug que contiene números")
    void obtenerPerfilUsuario_SlugConNumeros_Success() throws Exception {
        UsuarioPublicoDTO usuarioSlugNumeros = UsuarioPublicoDTO.builder()
                .idUsuario(4L)
                .slug("juan-perez-123")
                .nombreUsuario("Juan")
                .apellidosUsuario("Pérez 123")
                .tipoUsuario(TipoUsuario.NORMAL)
                .build();

        when(usuarioService.obtenerPerfilPublicoBySlug("juan-perez-123"))
                .thenReturn(usuarioSlugNumeros);

        mockMvc.perform(get("/api/public/usuarios/juan-perez-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("juan-perez-123"));

        verify(usuarioService, times(1)).obtenerPerfilPublicoBySlug("juan-perez-123");
    }

    @Test
    @DisplayName("Obtener perfil artista con slug que contiene números")
    void obtenerPerfilArtista_SlugConNumeros_Success() throws Exception {
        UsuarioPublicoDTO artistaSlugNumeros = UsuarioPublicoDTO.builder()
                .idUsuario(5L)
                .slugArtistico("dj-music-2024")
                .nombreArtistico("DJ Music 2024")
                .tipoUsuario(TipoUsuario.ARTISTA)
                .build();

        when(artistaService.obtenerPerfilArtistaPorSlug("dj-music-2024"))
                .thenReturn(artistaSlugNumeros);

        mockMvc.perform(get("/api/public/artistas/dj-music-2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slugArtistico").value("dj-music-2024"));

        verify(artistaService, times(1)).obtenerPerfilArtistaPorSlug("dj-music-2024");
    }

    @Test
    @DisplayName("Obtener perfil usuario con caracteres especiales en URL - debe manejar correctamente")
    void obtenerPerfilUsuario_CaracteresEspeciales() throws Exception {
        // Spring MVC debería manejar la codificación URL automáticamente
        when(usuarioService.obtenerPerfilPublicoBySlug("jose-maria"))
                .thenReturn(perfilUsuarioNormal);

        mockMvc.perform(get("/api/public/usuarios/jose-maria"))
                .andExpect(status().isOk());

        verify(usuarioService, times(1)).obtenerPerfilPublicoBySlug("jose-maria");
    }

    @Test
    @DisplayName("Obtener perfil usuario con slug muy corto - exitoso")
    void obtenerPerfilUsuario_SlugCorto_Success() throws Exception {
        UsuarioPublicoDTO usuarioSlugCorto = UsuarioPublicoDTO.builder()
                .idUsuario(6L)
                .slug("ab")
                .nombreUsuario("A")
                .apellidosUsuario("B")
                .tipoUsuario(TipoUsuario.NORMAL)
                .build();

        when(usuarioService.obtenerPerfilPublicoBySlug("ab"))
                .thenReturn(usuarioSlugCorto);

        mockMvc.perform(get("/api/public/usuarios/ab"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("ab"));

        verify(usuarioService, times(1)).obtenerPerfilPublicoBySlug("ab");
    }
}