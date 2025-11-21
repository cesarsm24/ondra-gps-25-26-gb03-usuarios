package com.ondra.users.exceptions;

import com.ondra.users.dto.ErrorDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para la API de usuarios.
 *
 * <p>Intercepta excepciones lanzadas por los controladores y genera respuestas
 * estandarizadas en formato {@link ErrorDTO} con código HTTP, mensaje descriptivo
 * y marca temporal.</p>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Crea respuestas de error estandarizadas.
     *
     * @param errorCode código de error en formato constante
     * @param message mensaje descriptivo del error
     * @param status código de estado HTTP
     * @param logWarning indica si debe registrar un warning en logs
     * @return respuesta con ErrorDTO estandarizado
     */
    private ResponseEntity<ErrorDTO> createErrorResponse(
            String errorCode,
            String message,
            HttpStatus status,
            boolean logWarning) {

        if (logWarning) {
            log.warn("⚠️ Error [{}]: {}", errorCode, message);
        }

        ErrorDTO error = ErrorDTO.builder()
                .error(errorCode)
                .message(message)
                .statusCode(status.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.status(status).body(error);
    }

    /**
     * Maneja excepciones cuando el email ya existe durante el registro.
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorDTO> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return createErrorResponse(
                "EMAIL_ALREADY_EXISTS",
                ex.getMessage(),
                HttpStatus.CONFLICT,
                false
        );
    }

    /**
     * Maneja excepciones cuando las credenciales de login son incorrectas.
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorDTO> handleInvalidCredentials(InvalidCredentialsException ex) {
        return createErrorResponse(
                "INVALID_CREDENTIALS",
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED,
                true
        );
    }

    /**
     * Maneja excepciones cuando la cuenta está inactiva.
     */
    @ExceptionHandler(AccountInactiveException.class)
    public ResponseEntity<ErrorDTO> handleAccountInactive(AccountInactiveException ex) {
        return createErrorResponse(
                "ACCOUNT_INACTIVE",
                ex.getMessage(),
                HttpStatus.FORBIDDEN,
                false
        );
    }

    /**
     * Maneja excepciones cuando el email no ha sido verificado.
     */
    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ErrorDTO> handleEmailNotVerified(EmailNotVerifiedException ex) {
        return createErrorResponse(
                "EMAIL_NOT_VERIFIED",
                ex.getMessage(),
                HttpStatus.FORBIDDEN,
                false
        );
    }

    /**
     * Maneja excepciones cuando el token de verificación es inválido.
     */
    @ExceptionHandler(InvalidVerificationTokenException.class)
    public ResponseEntity<ErrorDTO> handleInvalidVerificationToken(InvalidVerificationTokenException ex) {
        return createErrorResponse(
                "INVALID_VERIFICATION_TOKEN",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                false
        );
    }

    /**
     * Maneja excepciones cuando el token de Google es inválido.
     */
    @ExceptionHandler(InvalidGoogleTokenException.class)
    public ResponseEntity<ErrorDTO> handleInvalidGoogleToken(InvalidGoogleTokenException ex) {
        return createErrorResponse(
                "INVALID_GOOGLE_TOKEN",
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED,
                true
        );
    }

    /**
     * Maneja excepciones cuando el login con Google está deshabilitado.
     */
    @ExceptionHandler(GoogleLoginDisabledException.class)
    public ResponseEntity<ErrorDTO> handleGoogleLoginDisabled(GoogleLoginDisabledException ex) {
        return createErrorResponse(
                "GOOGLE_LOGIN_DISABLED",
                ex.getMessage(),
                HttpStatus.FORBIDDEN,
                false
        );
    }

    /**
     * Maneja excepciones cuando el refresh token es inválido.
     */
    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorDTO> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        return createErrorResponse(
                "INVALID_REFRESH_TOKEN",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                true
        );
    }

    /**
     * Maneja excepciones cuando el token de recuperación de contraseña es inválido.
     */
    @ExceptionHandler(InvalidPasswordResetTokenException.class)
    public ResponseEntity<ErrorDTO> handleInvalidPasswordResetToken(InvalidPasswordResetTokenException ex) {
        return createErrorResponse(
                "INVALID_PASSWORD_RESET_TOKEN",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                false
        );
    }

    /**
     * Maneja excepciones cuando no se encuentra un usuario.
     */
    @ExceptionHandler(UsuarioNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleUsuarioNotFound(UsuarioNotFoundException ex) {
        return createErrorResponse(
                "USER_NOT_FOUND",
                ex.getMessage(),
                HttpStatus.NOT_FOUND,
                false
        );
    }

    /**
     * Maneja excepciones cuando no se encuentra un artista.
     */
    @ExceptionHandler(ArtistaNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleArtistaNotFound(ArtistaNotFoundException ex) {
        return createErrorResponse(
                "ARTIST_NOT_FOUND",
                ex.getMessage(),
                HttpStatus.NOT_FOUND,
                false
        );
    }

    /**
     * Maneja excepciones cuando no se encuentra una red social.
     */
    @ExceptionHandler(RedSocialNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleRedSocialNotFound(RedSocialNotFoundException ex) {
        return createErrorResponse(
                "SOCIAL_NETWORK_NOT_FOUND",
                ex.getMessage(),
                HttpStatus.NOT_FOUND,
                false
        );
    }

    /**
     * Maneja excepciones cuando no se encuentra un método de pago de usuario.
     */
    @ExceptionHandler(MetodoPagoUsuarioNotFoundException.class)
    public ResponseEntity<ErrorDTO> handlePagoUsuarioNotFound(MetodoPagoUsuarioNotFoundException ex) {
        return createErrorResponse(
                "PAYMENT_NOT_FOUND",
                ex.getMessage(),
                HttpStatus.NOT_FOUND,
                false
        );
    }

    /**
     * Maneja excepciones cuando no se encuentra un método de cobro de artista.
     */
    @ExceptionHandler(MetodoCobroArtistaNotFoundException.class)
    public ResponseEntity<ErrorDTO> handlePagoArtistaNotFound(MetodoCobroArtistaNotFoundException ex) {
        return createErrorResponse(
                "PAYMENT_NOT_FOUND",
                ex.getMessage(),
                HttpStatus.NOT_FOUND,
                false
        );
    }

    /**
     * Maneja excepciones cuando un usuario intenta acceder a recursos sin permisos.
     */
    @ExceptionHandler(ForbiddenAccessException.class)
    public ResponseEntity<ErrorDTO> handleForbiddenAccess(ForbiddenAccessException ex) {
        return createErrorResponse(
                "FORBIDDEN",
                ex.getMessage(),
                HttpStatus.FORBIDDEN,
                false
        );
    }

    /**
     * Maneja excepciones cuando los datos proporcionados son inválidos.
     */
    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<ErrorDTO> handleInvalidData(InvalidDataException ex) {
        return createErrorResponse(
                "INVALID_DATA",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                false
        );
    }

    /**
     * Maneja excepciones cuando se utiliza un método de pago inválido.
     */
    @ExceptionHandler(InvalidPaymentMethodException.class)
    public ResponseEntity<ErrorDTO> handleInvalidPaymentMethod(InvalidPaymentMethodException ex) {
        return createErrorResponse(
                "INVALID_PAYMENT_METHOD",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                false
        );
    }

    /**
     * Maneja excepciones cuando un método de pago no pertenece al usuario o artista especificado.
     */
    @ExceptionHandler(PaymentMethodMismatchException.class)
    public ResponseEntity<ErrorDTO> handlePaymentMethodMismatch(PaymentMethodMismatchException ex) {
        return createErrorResponse(
                "PAYMENT_METHOD_MISMATCH",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                false
        );
    }

    /**
     * Maneja excepciones cuando una red social no pertenece al artista especificado.
     */
    @ExceptionHandler(SocialNetworkMismatchException.class)
    public ResponseEntity<ErrorDTO> handleSocialNetworkMismatch(SocialNetworkMismatchException ex) {
        return createErrorResponse(
                "SOCIAL_NETWORK_MISMATCH",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                false
        );
    }

    /**
     * Maneja excepciones cuando se intenta crear una red social duplicada.
     */
    @ExceptionHandler(DuplicateSocialNetworkException.class)
    public ResponseEntity<ErrorDTO> handleDuplicateSocialNetwork(DuplicateSocialNetworkException ex) {
        return createErrorResponse(
                "DUPLICATE_SOCIAL_NETWORK",
                ex.getMessage(),
                HttpStatus.CONFLICT,
                false
        );
    }

    /**
     * Maneja excepciones cuando se intenta realizar un seguimiento inválido.
     */
    @ExceptionHandler(InvalidFollowException.class)
    public ResponseEntity<ErrorDTO> handleInvalidFollow(InvalidFollowException ex) {
        return createErrorResponse(
                "INVALID_FOLLOW",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                true
        );
    }

    /**
     * Maneja excepciones cuando se intenta crear un seguimiento duplicado.
     */
    @ExceptionHandler(DuplicateFollowException.class)
    public ResponseEntity<ErrorDTO> handleDuplicateFollow(DuplicateFollowException ex) {
        return createErrorResponse(
                "DUPLICATE_FOLLOW",
                ex.getMessage(),
                HttpStatus.CONFLICT,
                true
        );
    }

    /**
     * Maneja excepciones cuando no se encuentra un seguimiento existente.
     */
    @ExceptionHandler(FollowNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleFollowNotFound(FollowNotFoundException ex) {
        return createErrorResponse(
                "FOLLOW_NOT_FOUND",
                ex.getMessage(),
                HttpStatus.NOT_FOUND,
                true
        );
    }

    /**
     * Maneja excepciones cuando no se proporciona ningún archivo.
     */
    @ExceptionHandler(NoFileProvidedException.class)
    public ResponseEntity<ErrorDTO> handleNoFileProvided(NoFileProvidedException ex) {
        return createErrorResponse(
                "NO_FILE_PROVIDED",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                false
        );
    }

    /**
     * Maneja excepciones cuando el formato de la imagen es inválido.
     */
    @ExceptionHandler(InvalidImageFormatException.class)
    public ResponseEntity<ErrorDTO> handleInvalidImageFormat(InvalidImageFormatException ex) {
        return createErrorResponse(
                "INVALID_IMAGE_FORMAT",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                false
        );
    }

    /**
     * Maneja excepciones cuando el tamaño de la imagen excede el límite permitido.
     */
    @ExceptionHandler(ImageSizeExceededException.class)
    public ResponseEntity<ErrorDTO> handleImageSizeExceeded(ImageSizeExceededException ex) {
        return createErrorResponse(
                "IMAGE_SIZE_EXCEEDED",
                ex.getMessage(),
                HttpStatus.PAYLOAD_TOO_LARGE,
                false
        );
    }

    /**
     * Maneja excepciones cuando falla la subida de imagen a Cloudinary.
     */
    @ExceptionHandler(ImageUploadFailedException.class)
    public ResponseEntity<ErrorDTO> handleImageUploadFailed(ImageUploadFailedException ex) {
        return createErrorResponse(
                "IMAGE_UPLOAD_FAILED",
                ex.getMessage(),
                HttpStatus.BAD_GATEWAY,
                true
        );
    }

    /**
     * Maneja excepciones cuando falla la eliminación de imagen de Cloudinary.
     */
    @ExceptionHandler(ImageDeletionFailedException.class)
    public ResponseEntity<ErrorDTO> handleImageDeletionFailed(ImageDeletionFailedException ex) {
        return createErrorResponse(
                "IMAGE_DELETION_FAILED",
                ex.getMessage(),
                HttpStatus.BAD_GATEWAY,
                true
        );
    }

    /**
     * Maneja errores de validación de argumentos de métodos con @Valid.
     * <p>Recopila los errores de cada campo y los retorna en un ErrorDTO unificado.</p>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("⚠️ Error de validación: {}", errors);

        ErrorDTO error = ErrorDTO.builder()
                .error("VALIDATION_ERROR")
                .message("Error de validación: " + errors)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja excepciones generales no controladas específicamente.
     * <p>Actúa como red de seguridad para cualquier excepción no manejada.</p>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleException(Exception ex) {
        log.error("❌ Error interno no controlado: {}", ex.getMessage(), ex);

        ErrorDTO error = ErrorDTO.builder()
                .error("INTERNAL_ERROR")
                .message("Ha ocurrido un error interno en el servidor")
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}