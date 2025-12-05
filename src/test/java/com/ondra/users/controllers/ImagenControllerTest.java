package com.ondra.users.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ondra.users.dto.ImagenResponseDTO;
import com.ondra.users.dto.SuccessfulResponseDTO;
import com.ondra.users.exceptions.*;
import com.ondra.users.repositories.RefreshTokenRepository;
import com.ondra.users.security.*;
import com.ondra.users.services.CloudinaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitarios de {@link ImagenController}.
 *
 * <p>Verifica la subida y eliminación de imágenes para usuarios y artistas.
 * Se prueban casos de éxito, errores de formato, tamaño, autenticación,
 * así como escenarios edge y errores de integración con Cloudinary.</p>
 */
@WebMvcTest(ImagenController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, ServiceTokenFilter.class, TestJwtHelper.class})
class ImagenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestJwtHelper testJwtHelper;

    @MockBean
    private CloudinaryService cloudinaryService;

    @MockBean
    private RefreshTokenRepository refreshTokenRepository;

    private String tokenUsuario;
    private String tokenArtista;
    private MockMultipartFile imagenValida;

    @BeforeEach
    void setUp() {
        tokenUsuario = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");
        tokenArtista = testJwtHelper.generarTokenPruebaArtista(2L, 10L, "artista@example.com");

        // Crear imagen válida para tests
        imagenValida = new MockMultipartFile(
                "file",
                "perfil.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "contenido de imagen".getBytes()
        );
    }

    // ==================== TESTS SUBIR IMAGEN USUARIO ====================

    @Test
    @DisplayName("Subir imagen de usuario - exitoso")
    void subirImagenUsuario_Success() throws Exception {
        String urlImagenSubida = "https://res.cloudinary.com/demo/image/upload/v123/usuarios/abc-123.jpg";

        when(cloudinaryService.subirImagen(any(MockMultipartFile.class), eq("usuarios")))
                .thenReturn(urlImagenSubida);

        mockMvc.perform(multipart("/api/imagenes/usuario")
                        .file(imagenValida)
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").value(urlImagenSubida))
                .andExpect(jsonPath("$.mensaje").value("Imagen de usuario subida correctamente"));

        verify(cloudinaryService, times(1))
                .subirImagen(any(MockMultipartFile.class), eq("usuarios"));
    }

    @Test
    @DisplayName("Subir imagen de usuario con formato PNG - exitoso")
    void subirImagenUsuario_PNG_Success() throws Exception {
        MockMultipartFile imagenPNG = new MockMultipartFile(
                "file",
                "foto.png",
                MediaType.IMAGE_PNG_VALUE,
                "contenido png".getBytes()
        );

        String urlImagenSubida = "https://res.cloudinary.com/demo/image/upload/v123/usuarios/def-456.png";

        when(cloudinaryService.subirImagen(any(MockMultipartFile.class), eq("usuarios")))
                .thenReturn(urlImagenSubida);

        mockMvc.perform(multipart("/api/imagenes/usuario")
                        .file(imagenPNG)
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").value(urlImagenSubida));

        verify(cloudinaryService, times(1))
                .subirImagen(any(MockMultipartFile.class), eq("usuarios"));
    }

    @Test
    @DisplayName("Subir imagen de usuario sin archivo - Bad Request")
    void subirImagenUsuario_SinArchivo_BadRequest() throws Exception {
        when(cloudinaryService.subirImagen(any(), eq("usuarios")))
                .thenThrow(new NoFileProvidedException("No se ha proporcionado ningún archivo"));

        MockMultipartFile archivoVacio = new MockMultipartFile(
                "file",
                "",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[0]
        );

        mockMvc.perform(multipart("/api/imagenes/usuario")
                        .file(archivoVacio)
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(cloudinaryService, times(1))
                .subirImagen(any(), eq("usuarios"));
    }

    @Test
    @DisplayName("Subir imagen de usuario con formato inválido - Bad Request")
    void subirImagenUsuario_FormatoInvalido_BadRequest() throws Exception {
        MockMultipartFile archivoPDF = new MockMultipartFile(
                "file",
                "documento.pdf",
                "application/pdf",
                "contenido pdf".getBytes()
        );

        when(cloudinaryService.subirImagen(any(MockMultipartFile.class), eq("usuarios")))
                .thenThrow(new InvalidImageFormatException("El archivo debe ser una imagen válida (JPG, PNG, WEBP)"));

        mockMvc.perform(multipart("/api/imagenes/usuario")
                        .file(archivoPDF)
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(cloudinaryService, times(1))
                .subirImagen(any(MockMultipartFile.class), eq("usuarios"));
    }

    @Test
    @DisplayName("Subir imagen de usuario excediendo tamaño - Payload Too Large")
    void subirImagenUsuario_TamanoExcedido_PayloadTooLarge() throws Exception {
        when(cloudinaryService.subirImagen(any(MockMultipartFile.class), eq("usuarios")))
                .thenThrow(new ImageSizeExceededException("La imagen no puede superar los 5MB"));

        mockMvc.perform(multipart("/api/imagenes/usuario")
                        .file(imagenValida)
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .with(csrf()))
                .andExpect(status().isPayloadTooLarge()); // 413

        verify(cloudinaryService, times(1))
                .subirImagen(any(MockMultipartFile.class), eq("usuarios"));
    }

    @Test
    @DisplayName("Subir imagen de usuario - error en Cloudinary - Bad Gateway")
    void subirImagenUsuario_ErrorCloudinary_BadGateway() throws Exception {
        when(cloudinaryService.subirImagen(any(MockMultipartFile.class), eq("usuarios")))
                .thenThrow(new ImageUploadFailedException("Error al subir la imagen a Cloudinary"));

        mockMvc.perform(multipart("/api/imagenes/usuario")
                        .file(imagenValida)
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .with(csrf()))
                .andExpect(status().isBadGateway()); // 502

        verify(cloudinaryService, times(1))
                .subirImagen(any(MockMultipartFile.class), eq("usuarios"));
    }

    @Test
    @DisplayName("Subir imagen de usuario sin autenticación - Forbidden")
    void subirImagenUsuario_SinAutenticacion_Forbidden() throws Exception {
        mockMvc.perform(multipart("/api/imagenes/usuario")
                        .file(imagenValida)
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(cloudinaryService, never()).subirImagen(any(), anyString());
    }

    // ==================== TESTS SUBIR IMAGEN ARTISTA ====================

    @Test
    @DisplayName("Subir imagen de artista - exitoso")
    void subirImagenArtista_Success() throws Exception {
        String urlImagenSubida = "https://res.cloudinary.com/demo/image/upload/v123/artistas/xyz-789.jpg";

        when(cloudinaryService.subirImagen(any(MockMultipartFile.class), eq("artistas")))
                .thenReturn(urlImagenSubida);

        mockMvc.perform(multipart("/api/imagenes/artista")
                        .file(imagenValida)
                        .header("Authorization", "Bearer " + tokenArtista)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").value(urlImagenSubida))
                .andExpect(jsonPath("$.mensaje").value("Imagen de artista subida correctamente"));

        verify(cloudinaryService, times(1))
                .subirImagen(any(MockMultipartFile.class), eq("artistas"));
    }

    @Test
    @DisplayName("Subir imagen de artista con formato WEBP - exitoso")
    void subirImagenArtista_WEBP_Success() throws Exception {
        MockMultipartFile imagenWEBP = new MockMultipartFile(
                "file",
                "foto.webp",
                "image/webp",
                "contenido webp".getBytes()
        );

        String urlImagenSubida = "https://res.cloudinary.com/demo/image/upload/v123/artistas/webp-123.webp";

        when(cloudinaryService.subirImagen(any(MockMultipartFile.class), eq("artistas")))
                .thenReturn(urlImagenSubida);

        mockMvc.perform(multipart("/api/imagenes/artista")
                        .file(imagenWEBP)
                        .header("Authorization", "Bearer " + tokenArtista)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").value(urlImagenSubida));

        verify(cloudinaryService, times(1))
                .subirImagen(any(MockMultipartFile.class), eq("artistas"));
    }

    @Test
    @DisplayName("Subir imagen de artista sin archivo - Bad Request")
    void subirImagenArtista_SinArchivo_BadRequest() throws Exception {
        when(cloudinaryService.subirImagen(any(), eq("artistas")))
                .thenThrow(new NoFileProvidedException("No se ha proporcionado ningún archivo"));

        MockMultipartFile archivoVacio = new MockMultipartFile(
                "file",
                "",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[0]
        );

        mockMvc.perform(multipart("/api/imagenes/artista")
                        .file(archivoVacio)
                        .header("Authorization", "Bearer " + tokenArtista)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(cloudinaryService, times(1))
                .subirImagen(any(), eq("artistas"));
    }

    @Test
    @DisplayName("Subir imagen de artista con formato inválido - Bad Request")
    void subirImagenArtista_FormatoInvalido_BadRequest() throws Exception {
        MockMultipartFile archivoGIF = new MockMultipartFile(
                "file",
                "animacion.gif",
                "image/gif",
                "contenido gif".getBytes()
        );

        when(cloudinaryService.subirImagen(any(MockMultipartFile.class), eq("artistas")))
                .thenThrow(new InvalidImageFormatException("El archivo debe ser una imagen válida (JPG, PNG, WEBP)"));

        mockMvc.perform(multipart("/api/imagenes/artista")
                        .file(archivoGIF)
                        .header("Authorization", "Bearer " + tokenArtista)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(cloudinaryService, times(1))
                .subirImagen(any(MockMultipartFile.class), eq("artistas"));
    }

    @Test
    @DisplayName("Subir imagen de artista excediendo tamaño - Payload Too Large")
    void subirImagenArtista_TamanoExcedido_PayloadTooLarge() throws Exception {
        when(cloudinaryService.subirImagen(any(MockMultipartFile.class), eq("artistas")))
                .thenThrow(new ImageSizeExceededException("La imagen no puede superar los 5MB"));

        mockMvc.perform(multipart("/api/imagenes/artista")
                        .file(imagenValida)
                        .header("Authorization", "Bearer " + tokenArtista)
                        .with(csrf()))
                .andExpect(status().isPayloadTooLarge()); // 413

        verify(cloudinaryService, times(1))
                .subirImagen(any(MockMultipartFile.class), eq("artistas"));
    }

    @Test
    @DisplayName("Subir imagen de artista sin autenticación - Forbidden")
    void subirImagenArtista_SinAutenticacion_Forbidden() throws Exception {
        mockMvc.perform(multipart("/api/imagenes/artista")
                        .file(imagenValida)
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(cloudinaryService, never()).subirImagen(any(), anyString());
    }

    // ==================== TESTS ELIMINAR IMAGEN ====================

    @Test
    @DisplayName("Eliminar imagen - exitoso")
    void eliminarImagen_Success() throws Exception {
        String urlImagen = "https://res.cloudinary.com/demo/image/upload/v123/usuarios/abc-123.jpg";

        doNothing().when(cloudinaryService).eliminarImagen(urlImagen);

        mockMvc.perform(delete("/api/imagenes")
                        .param("url", urlImagen)
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successful").value("Eliminación de imagen exitosa"))
                .andExpect(jsonPath("$.message").value("La imagen ha sido eliminada correctamente de Cloudinary"))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(cloudinaryService, times(1)).eliminarImagen(urlImagen);
    }

    @Test
    @DisplayName("Eliminar imagen de artista - exitoso")
    void eliminarImagenArtista_Success() throws Exception {
        String urlImagen = "https://res.cloudinary.com/demo/image/upload/v456/artistas/xyz-789.jpg";

        doNothing().when(cloudinaryService).eliminarImagen(urlImagen);

        mockMvc.perform(delete("/api/imagenes")
                        .param("url", urlImagen)
                        .header("Authorization", "Bearer " + tokenArtista)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successful").value("Eliminación de imagen exitosa"));

        verify(cloudinaryService, times(1)).eliminarImagen(urlImagen);
    }

    @Test
    @DisplayName("Eliminar imagen con URL larga - exitoso")
    void eliminarImagen_URLLarga_Success() throws Exception {
        String urlImagen = "https://res.cloudinary.com/demo/image/upload/v1234567890/usuarios/folder/subfolder/imagen-con-nombre-muy-largo-123-abc-def.jpg";

        doNothing().when(cloudinaryService).eliminarImagen(urlImagen);

        mockMvc.perform(delete("/api/imagenes")
                        .param("url", urlImagen)
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(cloudinaryService, times(1)).eliminarImagen(urlImagen);
    }

    @Test
    @DisplayName("Eliminar imagen - error en Cloudinary - Bad Gateway")
    void eliminarImagen_ErrorCloudinary_BadGateway() throws Exception {
        String urlImagen = "https://res.cloudinary.com/demo/image/upload/v123/usuarios/abc-123.jpg";

        doThrow(new ImageDeletionFailedException("Error al eliminar la imagen de Cloudinary"))
                .when(cloudinaryService).eliminarImagen(urlImagen);

        mockMvc.perform(delete("/api/imagenes")
                        .param("url", urlImagen)
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .with(csrf()))
                .andExpect(status().isBadGateway()); // 502

        verify(cloudinaryService, times(1)).eliminarImagen(urlImagen);
    }

    @Test
    @DisplayName("Eliminar imagen sin URL - Internal Server Error")
    void eliminarImagen_SinURL_InternalServerError() throws Exception {

        mockMvc.perform(delete("/api/imagenes")
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .with(csrf()))
                .andExpect(status().isInternalServerError()); // 500

        verify(cloudinaryService, never()).eliminarImagen(anyString());
    }

    @Test
    @DisplayName("Eliminar imagen sin autenticación - Forbidden")
    void eliminarImagen_SinAutenticacion_Forbidden() throws Exception {
        String urlImagen = "https://res.cloudinary.com/demo/image/upload/v123/usuarios/abc-123.jpg";

        mockMvc.perform(delete("/api/imagenes")
                        .param("url", urlImagen)
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(cloudinaryService, never()).eliminarImagen(anyString());
    }

    // ==================== TESTS CASOS EDGE ====================

    @Test
    @DisplayName("Subir imagen usuario con nombre especial - exitoso")
    void subirImagenUsuario_NombreEspecial_Success() throws Exception {
        MockMultipartFile imagenNombreEspecial = new MockMultipartFile(
                "file",
                "foto-perfil_2024 (1).jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "contenido".getBytes()
        );

        String urlImagenSubida = "https://res.cloudinary.com/demo/image/upload/v123/usuarios/uuid-123.jpg";

        when(cloudinaryService.subirImagen(any(MockMultipartFile.class), eq("usuarios")))
                .thenReturn(urlImagenSubida);

        mockMvc.perform(multipart("/api/imagenes/usuario")
                        .file(imagenNombreEspecial)
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").value(urlImagenSubida));

        verify(cloudinaryService, times(1))
                .subirImagen(any(MockMultipartFile.class), eq("usuarios"));
    }

    @Test
    @DisplayName("Eliminar imagen con URL que contiene caracteres especiales - exitoso")
    void eliminarImagen_URLConCaracteresEspeciales_Success() throws Exception {
        String urlImagen = "https://res.cloudinary.com/demo/image/upload/v123/usuarios/foto_perfil-2024%20(1).jpg";

        doNothing().when(cloudinaryService).eliminarImagen(urlImagen);

        mockMvc.perform(delete("/api/imagenes")
                        .param("url", urlImagen)
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(cloudinaryService, times(1)).eliminarImagen(urlImagen);
    }

    @Test
    @DisplayName("Usuario normal puede subir imagen - sin restricción de tipo")
    void usuarioNormal_PuedeSubirImagen_Success() throws Exception {
        String urlImagenSubida = "https://res.cloudinary.com/demo/image/upload/v123/usuarios/normal-123.jpg";

        when(cloudinaryService.subirImagen(any(MockMultipartFile.class), eq("usuarios")))
                .thenReturn(urlImagenSubida);

        mockMvc.perform(multipart("/api/imagenes/usuario")
                        .file(imagenValida)
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .with(csrf()))
                .andExpect(status().isCreated());

        verify(cloudinaryService, times(1))
                .subirImagen(any(MockMultipartFile.class), eq("usuarios"));
    }

    @Test
    @DisplayName("Artista puede subir imagen de usuario también - exitoso")
    void artista_PuedeSubirImagenUsuario_Success() throws Exception {
        String urlImagenSubida = "https://res.cloudinary.com/demo/image/upload/v123/usuarios/artista-user-123.jpg";

        when(cloudinaryService.subirImagen(any(MockMultipartFile.class), eq("usuarios")))
                .thenReturn(urlImagenSubida);

        mockMvc.perform(multipart("/api/imagenes/usuario")
                        .file(imagenValida)
                        .header("Authorization", "Bearer " + tokenArtista)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").value(urlImagenSubida));

        verify(cloudinaryService, times(1))
                .subirImagen(any(MockMultipartFile.class), eq("usuarios"));
    }
}