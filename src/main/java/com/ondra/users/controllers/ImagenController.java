package com.ondra.users.controllers;

import com.ondra.users.exceptions.*;
import com.ondra.users.dto.ImagenResponseDTO;
import com.ondra.users.dto.SuccessfulResponseDTO;
import com.ondra.users.services.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * Controlador REST para gestión de imágenes en Cloudinary.
 *
 * <p>Permite subir y eliminar imágenes de perfil para usuarios y artistas.
 * Todas las operaciones requieren autenticación JWT.</p>
 *
 * <p>Restricciones: formatos JPG/PNG/WEBP, máximo 5MB, transformación automática a 500x500px.</p>
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ImagenController {

    private final CloudinaryService cloudinaryService;

    /**
     * Sube una imagen de perfil para un usuario.
     *
     * <p>La imagen se almacena en la carpeta images/usuarios con transformación
     * automática a 500x500px.</p>
     *
     * @param file Archivo de imagen (máximo 5MB, formatos: JPG, PNG, WEBP)
     * @param authentication Autenticación del usuario
     * @return URL de la imagen subida
     * @throws NoFileProvidedException Si no se proporciona archivo
     * @throws InvalidImageFormatException Si el formato es inválido
     * @throws ImageSizeExceededException Si excede el tamaño permitido
     * @throws ImageUploadFailedException Si falla la subida
     */
    @PostMapping(value = "/imagenes/usuario",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ImagenResponseDTO> subirImagenUsuario(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        String imageUrl = cloudinaryService.subirImagen(file, "usuarios");

        ImagenResponseDTO response = ImagenResponseDTO.builder()
                .url(imageUrl)
                .mensaje("Imagen de usuario subida correctamente")
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Sube una imagen de perfil para un artista.
     *
     * <p>La imagen se almacena en la carpeta images/artistas con transformación
     * automática a 500x500px.</p>
     *
     * @param file Archivo de imagen (máximo 5MB, formatos: JPG, PNG, WEBP)
     * @param authentication Autenticación del usuario
     * @return URL de la imagen subida
     * @throws NoFileProvidedException Si no se proporciona archivo
     * @throws InvalidImageFormatException Si el formato es inválido
     * @throws ImageSizeExceededException Si excede el tamaño permitido
     * @throws ImageUploadFailedException Si falla la subida
     */
    @PostMapping(value = "/imagenes/artista",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ImagenResponseDTO> subirImagenArtista(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        String imageUrl = cloudinaryService.subirImagen(file, "artistas");

        ImagenResponseDTO response = ImagenResponseDTO.builder()
                .url(imageUrl)
                .mensaje("Imagen de artista subida correctamente")
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Elimina una imagen de Cloudinary.
     *
     * <p>Solo elimina la imagen del servicio de almacenamiento. La actualización
     * de referencias en base de datos debe realizarse en los endpoints específicos.</p>
     *
     * @param imageUrl URL completa de la imagen a eliminar
     * @param authentication Autenticación del usuario
     * @return Confirmación de la eliminación
     * @throws ImageDeletionFailedException Si falla la eliminación
     */
    @DeleteMapping(value = "/imagenes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SuccessfulResponseDTO> eliminarImagen(
            @RequestParam("url") String imageUrl,
            Authentication authentication) {

        cloudinaryService.eliminarImagen(imageUrl);

        SuccessfulResponseDTO response = SuccessfulResponseDTO.builder()
                .successful("Eliminación de imagen exitosa")
                .message("La imagen ha sido eliminada correctamente de Cloudinary")
                .statusCode(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.ok(response);
    }
}