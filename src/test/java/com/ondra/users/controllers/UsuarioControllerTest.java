package com.ondra.users.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ondra.users.dto.*;
import com.ondra.users.exceptions.*;
import com.ondra.users.models.enums.TipoUsuario;
import com.ondra.users.repositories.RefreshTokenRepository;
import com.ondra.users.security.*;
import com.ondra.users.services.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitarios para {@link UsuarioController}
 *
 * <p>
 * Esta clase cubre pruebas de:
 * </p>
 *
 * <ul>
 *   <li>Registro de usuarios (incluyendo manejo de emails duplicados)</li>
 *   <li>Obtención de información de usuarios (propio perfil, otro usuario, usuario inexistente)</li>
 *   <li>Edición de usuarios y manejo de accesos prohibidos</li>
 *   <li>Cambio de contraseña y validación de la contraseña actual</li>
 *   <li>Eliminación de usuarios y control de accesos</li>
 *   <li>Logout global de todas las sesiones activas</li>
 *   <li>Marcado de onboarding como completado</li>
 *   <li>Login de usuarios con credenciales válidas e inválidas</li>
 *   <li>Obtención de estadísticas globales de usuarios y artistas</li>
 * </ul>
 */
@WebMvcTest(UsuarioController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, ServiceTokenFilter.class, TestJwtHelper.class})
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestJwtHelper testJwtHelper;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private RefreshTokenRepository refreshTokenRepository;

    private UsuarioDTO usuarioDTO;
    private AuthResponseDTO authResponseDTO;

    @BeforeEach
    void setUp() {
        usuarioDTO = UsuarioDTO.builder()
                .idUsuario(1L)
                .nombreUsuario("Juan")
                .apellidosUsuario("Pérez")
                .emailUsuario("juan@example.com")
                .fotoPerfil("http://example.com/foto.jpg")
                .tipoUsuario(TipoUsuario.NORMAL)
                .emailVerificado(true)
                .activo(true)
                .permiteGoogle(true)
                .slug("juan-perez")
                .build();

        authResponseDTO = AuthResponseDTO.builder()
                .token("access-token-123")
                .refreshToken("refresh-token-456")
                .usuario(usuarioDTO)
                .tipo("Bearer")
                .build();
    }

    // ==================== TESTS REGISTRO ====================

    @Test
    @DisplayName("Registro exitoso de usuario")
    void registrarUsuario_Success() throws Exception {
        RegistroUsuarioDTO registroDTO = RegistroUsuarioDTO.builder()
                .nombreUsuario("Juan")
                .apellidosUsuario("Pérez")
                .emailUsuario("juan@example.com")
                .passwordUsuario("Password123!")
                .tipoUsuario(TipoUsuario.NORMAL)
                .build();

        when(usuarioService.registrarUsuario(any(RegistroUsuarioDTO.class)))
                .thenReturn(usuarioDTO);

        mockMvc.perform(post("/api/usuarios")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idUsuario").value(1))
                .andExpect(jsonPath("$.nombreUsuario").value("Juan"))
                .andExpect(jsonPath("$.emailUsuario").value("juan@example.com"));

        verify(usuarioService, times(1)).registrarUsuario(any(RegistroUsuarioDTO.class));
    }

    @Test
    @DisplayName("Registro con email duplicado - Bad Request")
    void registrarUsuario_EmailDuplicado() throws Exception {
        RegistroUsuarioDTO registroDTO = RegistroUsuarioDTO.builder()
                .nombreUsuario("Juan")
                .apellidosUsuario("Pérez")
                .emailUsuario("juan@example.com")
                .passwordUsuario("Password123!")
                .tipoUsuario(TipoUsuario.NORMAL)
                .build();

        when(usuarioService.registrarUsuario(any(RegistroUsuarioDTO.class)))
                .thenThrow(new EmailAlreadyExistsException("El email ya está registrado"));

        mockMvc.perform(post("/api/usuarios")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroDTO)))
                .andExpect(status().isBadRequest());

        verify(usuarioService, times(1)).registrarUsuario(any(RegistroUsuarioDTO.class));
    }

    // ==================== TESTS OBTENER USUARIO ====================

    @Test
    @DisplayName("Obtener usuario exitoso - propio perfil")
    void obtenerUsuario_Success() throws Exception {
        // Generar token JWT real para usuario 1
        String token = testJwtHelper.generarTokenPrueba(1L, "juan@example.com");

        when(usuarioService.obtenerUsuario(1L, 1L)).thenReturn(usuarioDTO);

        mockMvc.perform(get("/api/usuarios/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(1))
                .andExpect(jsonPath("$.emailUsuario").value("juan@example.com"));

        verify(usuarioService, times(1)).obtenerUsuario(1L, 1L);
    }

    @Test
    @DisplayName("Obtener usuario de otro - Forbidden")
    void obtenerUsuario_OtroUsuario_Forbidden() throws Exception {
        // Usuario 1 intenta acceder al usuario 2
        String token = testJwtHelper.generarTokenPrueba(1L, "juan@example.com");

        when(usuarioService.obtenerUsuario(2L, 1L))
                .thenThrow(new ForbiddenAccessException("No puedes acceder al perfil de otro usuario"));

        mockMvc.perform(get("/api/usuarios/2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        verify(usuarioService, times(1)).obtenerUsuario(2L, 1L);
    }

    @Test
    @DisplayName("Obtener usuario inexistente - Not Found")
    void obtenerUsuario_NoExiste() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "juan@example.com");

        when(usuarioService.obtenerUsuario(999L, 1L))
                .thenThrow(new UsuarioNotFoundException(999L));

        mockMvc.perform(get("/api/usuarios/999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        verify(usuarioService, times(1)).obtenerUsuario(999L, 1L);
    }

    // ==================== TESTS EDITAR USUARIO ====================

    @Test
    @DisplayName("Editar usuario exitoso")
    void editarUsuario_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "juan@example.com");

        EditarUsuarioDTO editarDTO = EditarUsuarioDTO.builder()
                .nombreUsuario("Juan Carlos")
                .apellidosUsuario("Pérez García")
                .build();

        UsuarioDTO usuarioActualizado = UsuarioDTO.builder()
                .idUsuario(1L)
                .nombreUsuario("Juan Carlos")
                .apellidosUsuario("Pérez García")
                .emailUsuario("juan@example.com")
                .tipoUsuario(TipoUsuario.NORMAL)
                .build();

        when(usuarioService.editarUsuario(eq(1L), any(EditarUsuarioDTO.class), eq(1L)))
                .thenReturn(usuarioActualizado);

        mockMvc.perform(put("/api/usuarios/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editarDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreUsuario").value("Juan Carlos"))
                .andExpect(jsonPath("$.apellidosUsuario").value("Pérez García"));

        verify(usuarioService, times(1)).editarUsuario(eq(1L), any(EditarUsuarioDTO.class), eq(1L));
    }

    @Test
    @DisplayName("Editar usuario de otro - Forbidden")
    void editarUsuario_OtroUsuario_Forbidden() throws Exception {
        // Usuario 2 intenta editar usuario 1
        String token = testJwtHelper.generarTokenPrueba(2L, "otro@example.com");

        EditarUsuarioDTO editarDTO = EditarUsuarioDTO.builder()
                .nombreUsuario("Intento Hackeo")
                .build();

        when(usuarioService.editarUsuario(eq(1L), any(EditarUsuarioDTO.class), eq(2L)))
                .thenThrow(new ForbiddenAccessException("No puedes editar el perfil de otro usuario"));

        mockMvc.perform(put("/api/usuarios/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editarDTO)))
                .andExpect(status().isForbidden());

        verify(usuarioService, times(1)).editarUsuario(eq(1L), any(EditarUsuarioDTO.class), eq(2L));
    }

    // ==================== TESTS CAMBIAR PASSWORD ====================

    @Test
    @DisplayName("Cambiar contraseña exitoso")
    void cambiarPassword_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "juan@example.com");

        CambiarPasswordDTO dto = CambiarPasswordDTO.builder()
                .passwordActual("OldPassword123!")
                .nuevaPassword("NewPassword123!")
                .build();

        doNothing().when(usuarioService).cambiarPassword(eq(1L), any(CambiarPasswordDTO.class), eq(1L));

        mockMvc.perform(put("/api/usuarios/1/cambiar-password")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Contraseña cambiada correctamente")));

        verify(usuarioService, times(1)).cambiarPassword(eq(1L), any(CambiarPasswordDTO.class), eq(1L));
    }

    @Test
    @DisplayName("Cambiar contraseña con password actual incorrecta")
    void cambiarPassword_PasswordActualIncorrecta() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "juan@example.com");

        CambiarPasswordDTO dto = CambiarPasswordDTO.builder()
                .passwordActual("WrongPassword")
                .nuevaPassword("NewPassword123!")
                .build();

        doThrow(new InvalidCredentialsException("La contraseña actual es incorrecta"))
                .when(usuarioService).cambiarPassword(eq(1L), any(CambiarPasswordDTO.class), eq(1L));

        mockMvc.perform(put("/api/usuarios/1/cambiar-password")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());

        verify(usuarioService, times(1)).cambiarPassword(eq(1L), any(CambiarPasswordDTO.class), eq(1L));
    }

    // ==================== TESTS ELIMINAR USUARIO ====================

    @Test
    @DisplayName("Eliminar usuario exitoso")
    void eliminarUsuario_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "juan@example.com");

        doNothing().when(usuarioService).eliminarUsuario(1L, 1L);

        mockMvc.perform(delete("/api/usuarios/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        verify(usuarioService, times(1)).eliminarUsuario(1L, 1L);
    }

    @Test
    @DisplayName("Eliminar usuario de otro - Forbidden")
    void eliminarUsuario_OtroUsuario_Forbidden() throws Exception {
        // Usuario 2 intenta eliminar usuario 1
        String token = testJwtHelper.generarTokenPrueba(2L, "otro@example.com");

        doThrow(new ForbiddenAccessException("No puedes eliminar la cuenta de otro usuario"))
                .when(usuarioService).eliminarUsuario(1L, 2L);

        mockMvc.perform(delete("/api/usuarios/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        verify(usuarioService, times(1)).eliminarUsuario(1L, 2L);
    }

    // ==================== TESTS LOGOUT GLOBAL ====================

    @Test
    @DisplayName("Logout global exitoso - todas las sesiones")
    void logoutAll_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "juan@example.com");

        doNothing().when(usuarioService).logoutGlobal(1L);

        mockMvc.perform(post("/api/usuarios/logout-all")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        verify(usuarioService, times(1)).logoutGlobal(1L);
    }

    // ==================== TESTS ONBOARDING ====================

    @Test
    @DisplayName("Marcar onboarding completado exitoso")
    void marcarOnboardingCompletado_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "juan@example.com");

        doNothing().when(usuarioService).marcarOnboardingCompletado(1L);

        mockMvc.perform(patch("/api/usuarios/1/onboarding-completado")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        verify(usuarioService, times(1)).marcarOnboardingCompletado(1L);
    }

    @Test
    @DisplayName("Marcar onboarding de otro usuario - Forbidden")
    void marcarOnboardingCompletado_OtroUsuario_Forbidden() throws Exception {
        // Usuario 2 intenta marcar onboarding de usuario 1
        String token = testJwtHelper.generarTokenPrueba(2L, "otro@example.com");

        mockMvc.perform(patch("/api/usuarios/1/onboarding-completado")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).marcarOnboardingCompletado(anyLong());
    }

    @Test
    @DisplayName("Marcar onboarding sin autenticación - Forbidden (sin token)")
    void marcarOnboardingCompletado_SinAutenticacion() throws Exception {
        // Sin token JWT, Spring Security devuelve 403 Forbidden
        mockMvc.perform(patch("/api/usuarios/1/onboarding-completado")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).marcarOnboardingCompletado(anyLong());
    }

    // ==================== TESTS LOGIN ====================

    @Test
    @DisplayName("Login exitoso con credenciales válidas")
    void loginUsuario_Success() throws Exception {
        LoginUsuarioDTO loginDTO = new LoginUsuarioDTO("juan@example.com", "Password123!");
        when(usuarioService.loginUsuario(any(LoginUsuarioDTO.class)))
                .thenReturn(authResponseDTO);

        mockMvc.perform(post("/api/usuarios/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-token-123"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-456"))
                .andExpect(jsonPath("$.usuario.idUsuario").value(1))
                .andExpect(jsonPath("$.usuario.emailUsuario").value("juan@example.com"))
                .andExpect(jsonPath("$.tipo").value("Bearer"));

        verify(usuarioService, times(1)).loginUsuario(any(LoginUsuarioDTO.class));
    }

    @Test
    @DisplayName("Login con credenciales incorrectas")
    void loginUsuario_CredencialesIncorrectas() throws Exception {
        LoginUsuarioDTO loginDTO = new LoginUsuarioDTO("juan@example.com", "WrongPassword");
        when(usuarioService.loginUsuario(any(LoginUsuarioDTO.class)))
                .thenThrow(new InvalidCredentialsException("Credenciales inválidas"));

        mockMvc.perform(post("/api/usuarios/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());

        verify(usuarioService, times(1)).loginUsuario(any(LoginUsuarioDTO.class));
    }

    // ==================== TESTS ESTADÍSTICAS ====================

    @Test
    @DisplayName("Obtener estadísticas globales")
    void obtenerStats_Success() throws Exception {
        EstadisticasGlobalesDTO stats = EstadisticasGlobalesDTO.builder()
                .totalUsuarios(150L)
                .totalArtistas(25L)
                .build();

        when(usuarioService.obtenerStats()).thenReturn(stats);

        mockMvc.perform(get("/api/usuarios/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsuarios").value(150))
                .andExpect(jsonPath("$.totalArtistas").value(25));

        verify(usuarioService, times(1)).obtenerStats();
    }
}