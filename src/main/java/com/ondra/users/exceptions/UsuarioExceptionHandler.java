package com.ondra.users.exceptions;

import com.ondra.users.dto.ErrorDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UsuarioExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorDTO> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return createErrorResponse("EMAIL_ALREADY_EXISTS", ex.getMessage(), HttpStatus.BAD_REQUEST, false);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorDTO> handleInvalidCredentials(InvalidCredentialsException ex) {
        return createErrorResponse("INVALID_CREDENTIALS", ex.getMessage(), HttpStatus.UNAUTHORIZED, true);
    }

    @ExceptionHandler(AccountInactiveException.class)
    public ResponseEntity<ErrorDTO> handleAccountInactive(AccountInactiveException ex) {
        return createErrorResponse("ACCOUNT_INACTIVE", ex.getMessage(), HttpStatus.FORBIDDEN, false);
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ErrorDTO> handleEmailNotVerified(EmailNotVerifiedException ex) {
        return createErrorResponse("EMAIL_NOT_VERIFIED", ex.getMessage(), HttpStatus.FORBIDDEN, false);
    }
}