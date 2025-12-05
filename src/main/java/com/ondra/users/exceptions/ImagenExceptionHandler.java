package com.ondra.users.exceptions;

import com.ondra.users.dto.ErrorDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ImagenExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler(InvalidImageFormatException.class)
    public ResponseEntity<ErrorDTO> handleInvalidImageFormat(InvalidImageFormatException ex) {
        return createErrorResponse("INVALID_IMAGE_FORMAT", ex.getMessage(), HttpStatus.BAD_REQUEST, false);
    }

    @ExceptionHandler(ImageSizeExceededException.class)
    public ResponseEntity<ErrorDTO> handleImageSizeExceeded(ImageSizeExceededException ex) {
        return createErrorResponse("IMAGE_SIZE_EXCEEDED", ex.getMessage(), HttpStatus.PAYLOAD_TOO_LARGE, false);
    }

    @ExceptionHandler(ImageUploadFailedException.class)
    public ResponseEntity<ErrorDTO> handleImageUploadFailed(ImageUploadFailedException ex) {
        return createErrorResponse("IMAGE_UPLOAD_FAILED", ex.getMessage(), HttpStatus.BAD_GATEWAY, true);
    }

    @ExceptionHandler(ImageDeletionFailedException.class)
    public ResponseEntity<ErrorDTO> handleImageDeletionFailed(ImageDeletionFailedException ex) {
        return createErrorResponse("IMAGE_DELETION_FAILED", ex.getMessage(), HttpStatus.BAD_GATEWAY, true);
    }
}