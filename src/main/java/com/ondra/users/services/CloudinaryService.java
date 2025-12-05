package com.ondra.users.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.utils.ObjectUtils;
import com.ondra.users.exceptions.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio para gestión de imágenes en Cloudinary.
 *
 * <p>Proporciona operaciones de subida, eliminación y validación de imágenes.
 * Las imágenes se redimensionan automáticamente a 500x500px con optimización de calidad.</p>
 *
 * <p>Límites configurados:</p>
 * <ul>
 *   <li>Formatos soportados: JPG, PNG, WEBP</li>
 *   <li>Tamaño máximo: 5MB</li>
 *   <li>Transformación: 500x500px, crop fill, calidad automática</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private static final String PATH_SEPARATOR = "/";
    private static final String UPLOAD_SEGMENT = "/upload/";
    private static final int UPLOAD_SEGMENT_LENGTH = 8;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024L; // 5MB

    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder}")
    private String folder;

    /**
     * Sube una imagen a Cloudinary aplicando transformaciones.
     *
     * <p>La imagen se redimensiona a 500x500px con crop fill y calidad optimizada automáticamente.</p>
     *
     * @param file archivo de imagen a subir
     * @param carpeta subcarpeta destino dentro del folder principal
     * @return URL pública de la imagen subida
     * @throws NoFileProvidedException si el archivo es nulo o vacío
     * @throws InvalidImageFormatException si el formato no está permitido
     * @throws ImageSizeExceededException si supera el límite de 5MB
     * @throws ImageUploadFailedException si falla la subida
     */
    public String subirImagen(MultipartFile file, String carpeta) {
        log.debug("Iniciando subida de imagen a carpeta: {}", carpeta);

        if (file == null || file.isEmpty()) {
            log.warn("Intento de subir imagen sin proporcionar archivo");
            throw new NoFileProvidedException("No se ha proporcionado ningún archivo");
        }

        if (!esImagenValida(file)) {
            log.warn("Formato inválido: {}", file.getContentType());
            throw new InvalidImageFormatException(
                    "El archivo debe ser una imagen válida (JPG, PNG, WEBP)"
            );
        }

        if (!esTamanoValido(file)) {
            log.warn("Tamaño excedido: {} bytes", file.getSize());
            throw new ImageSizeExceededException("La imagen no puede superar los 5MB");
        }

        try {
            String publicId = generarPublicId();
            String folderPath = construirRutaCarpeta(carpeta);

            log.debug("Subiendo imagen con public_id: {} a carpeta: {}", publicId, folderPath);

            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "folder", folderPath,
                            "resource_type", "image",
                            "overwrite", true,
                            "transformation", new com.cloudinary.Transformation()
                                    .width(500).height(500)
                                    .crop("fill")
                                    .quality("auto")
                    )
            );

            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("✅ Imagen subida exitosamente: {}", secureUrl);

            return secureUrl;

        } catch (IOException e) {
            log.error("❌ Error al subir imagen: {}", e.getMessage(), e);
            throw new ImageUploadFailedException("Error al subir la imagen a Cloudinary", e);
        }
    }

    /**
     * Elimina una imagen de Cloudinary.
     *
     * <p>Extrae el public_id de la URL y elimina el recurso.
     * Si la URL es inválida o la imagen no existe, registra un warning sin lanzar excepción.</p>
     *
     * @param imageUrl URL completa de la imagen a eliminar
     * @throws ImageDeletionFailedException si falla la eliminación
     */
    public void eliminarImagen(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            log.warn("Intento de eliminar imagen con URL nula o vacía");
            return;
        }

        String publicId = extraerPublicId(imageUrl);
        if (publicId == null) {
            log.warn("No se pudo extraer public_id de la URL: {}", imageUrl);
            return;
        }

        try {
            log.debug("Eliminando imagen con public_id: {}", publicId);

            Map<String, Object> result = cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.emptyMap()
            );

            String resultStatus = (String) result.get("result");

            if ("ok".equals(resultStatus)) {
                log.info("✅ Imagen eliminada: {}", publicId);
            } else {
                log.warn("⚠️ Resultado inesperado al eliminar: {} - Status: {}",
                        publicId, resultStatus);
            }

        } catch (IOException e) {
            log.error("❌ Error al eliminar imagen: {}", e.getMessage(), e);
            throw new ImageDeletionFailedException(
                    "Error al eliminar la imagen de Cloudinary", e
            );
        }
    }

    /**
     * Extrae el public_id de una URL de Cloudinary.
     *
     * <p>Formato esperado: https://res.cloudinary.com/.../upload/v12345/folder/image.jpg</p>
     * <p>Extrae: folder/image (sin versión ni extensión)</p>
     *
     * @param imageUrl URL completa de la imagen
     * @return public_id extraído o null si no se puede procesar
     */
    private String extraerPublicId(String imageUrl) {
        try {
            int uploadIndex = imageUrl.indexOf(UPLOAD_SEGMENT);
            if (uploadIndex == -1) {
                log.warn("URL no contiene '/upload/': {}", imageUrl);
                return null;
            }

            String afterUpload = imageUrl.substring(uploadIndex + UPLOAD_SEGMENT_LENGTH);
            int versionEnd = afterUpload.indexOf(PATH_SEPARATOR);

            if (versionEnd == -1) {
                log.warn("URL sin formato de versión: {}", imageUrl);
                return null;
            }

            String pathWithExtension = afterUpload.substring(versionEnd + 1);
            int lastDot = pathWithExtension.lastIndexOf(".");

            String publicId = lastDot != -1
                    ? pathWithExtension.substring(0, lastDot)
                    : pathWithExtension;

            log.debug("Public ID extraído: {}", publicId);
            return publicId;

        } catch (Exception e) {
            log.error("Error al extraer public_id de la URL: {}", imageUrl, e);
            return null;
        }
    }

    /**
     * Genera un identificador único para la imagen.
     *
     * @return UUID en formato string
     */
    private String generarPublicId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Valida el formato de imagen.
     *
     * <p>Formatos aceptados: image/jpeg, image/jpg, image/png, image/webp</p>
     *
     * @param file archivo a validar
     * @return true si el formato es válido
     */
    public boolean esImagenValida(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }

        return contentType.equals("image/jpeg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/webp");
    }

    /**
     * Valida el tamaño del archivo.
     *
     * @param file archivo a validar
     * @return true si el tamaño no excede 5MB
     */
    public boolean esTamanoValido(MultipartFile file) {
        return file != null && file.getSize() <= MAX_FILE_SIZE;
    }

    /**
     * Elimina todas las imágenes de una carpeta en Cloudinary.
     *
     * <p>Operación utilizada principalmente para limpieza de datos de seeding.
     * Procesa hasta 500 recursos por ejecución.</p>
     *
     * @param carpeta subcarpeta a limpiar
     * @return número de imágenes eliminadas
     */
    public int limpiarCarpeta(String carpeta) {
        String folderPath = construirRutaCarpeta(carpeta);
        int imagenesEliminadas = 0;

        try {
            log.info("🧹 Iniciando limpieza de carpeta: {}", folderPath);

            ApiResponse result = cloudinary.api().resources(
                    ObjectUtils.asMap(
                            "type", "upload",
                            "prefix", folderPath,
                            "max_results", 500
                    )
            );

            List<Map<String, Object>> resources =
                    (List<Map<String, Object>>) result.get("resources");

            if (resources == null || resources.isEmpty()) {
                log.info("No se encontraron imágenes en: {}", folderPath);
                return 0;
            }

            log.info("📦 Imágenes encontradas: {}", resources.size());

            for (Map<String, Object> resource : resources) {
                imagenesEliminadas += eliminarRecurso(resource);
            }

            log.info("✅ Limpieza completada: {} imágenes eliminadas", imagenesEliminadas);

        } catch (Exception e) {
            log.error("❌ Error durante limpieza de carpeta {}: {}",
                    folderPath, e.getMessage(), e);
        }

        return imagenesEliminadas;
    }

    /**
     * Elimina un recurso individual durante la limpieza de carpeta.
     *
     * @param resource mapa con información del recurso
     * @return 1 si se eliminó correctamente, 0 en caso contrario
     */
    private int eliminarRecurso(Map<String, Object> resource) {
        String publicId = (String) resource.get("public_id");

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.debug("🗑️ Imagen eliminada: {}", publicId);
            return 1;

        } catch (Exception e) {
            log.warn("⚠️ No se pudo eliminar: {} - Error: {}",
                    publicId, e.getMessage());
            return 0;
        }
    }

    /**
     * Construye la ruta completa de la carpeta concatenando el folder base con la subcarpeta.
     *
     * @param carpeta subcarpeta destino
     * @return ruta completa de la carpeta
     */
    private String construirRutaCarpeta(String carpeta) {
        return folder + PATH_SEPARATOR + carpeta;
    }
}