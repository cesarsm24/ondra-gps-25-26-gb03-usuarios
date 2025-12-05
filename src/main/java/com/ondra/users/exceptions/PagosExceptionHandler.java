package com.ondra.users.exceptions;

import com.ondra.users.dto.ErrorDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PagosExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler(MetodoPagoUsuarioNotFoundException.class)
    public ResponseEntity<ErrorDTO> handlePagoUsuarioNotFound(MetodoPagoUsuarioNotFoundException ex) {
        return createErrorResponse("PAYMENT_NOT_FOUND", ex.getMessage(), HttpStatus.NOT_FOUND, false);
    }

    @ExceptionHandler(InvalidPaymentMethodException.class)
    public ResponseEntity<ErrorDTO> handleInvalidPaymentMethod(InvalidPaymentMethodException ex) {
        return createErrorResponse("INVALID_PAYMENT_METHOD", ex.getMessage(), HttpStatus.BAD_REQUEST, false);
    }

    @ExceptionHandler(PaymentMethodMismatchException.class)
    public ResponseEntity<ErrorDTO> handlePaymentMethodMismatch(PaymentMethodMismatchException ex) {
        return createErrorResponse("PAYMENT_METHOD_MISMATCH", ex.getMessage(), HttpStatus.BAD_REQUEST, false);
    }
}