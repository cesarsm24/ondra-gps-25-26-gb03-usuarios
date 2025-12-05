package com.ondra.users.exceptions;

import com.ondra.users.dto.ErrorDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ArtistaExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler(ArtistaNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleArtistaNotFound(ArtistaNotFoundException ex) {
        return createErrorResponse("ARTIST_NOT_FOUND", ex.getMessage(), HttpStatus.NOT_FOUND, false);
    }

    @ExceptionHandler(SocialNetworkMismatchException.class)
    public ResponseEntity<ErrorDTO> handleSocialNetworkMismatch(SocialNetworkMismatchException ex) {
        return createErrorResponse("SOCIAL_NETWORK_MISMATCH", ex.getMessage(), HttpStatus.BAD_REQUEST, false);
    }

    @ExceptionHandler(DuplicateSocialNetworkException.class)
    public ResponseEntity<ErrorDTO> handleDuplicateSocialNetwork(DuplicateSocialNetworkException ex) {
        return createErrorResponse("DUPLICATE_SOCIAL_NETWORK", ex.getMessage(), HttpStatus.CONFLICT, false);
    }
}