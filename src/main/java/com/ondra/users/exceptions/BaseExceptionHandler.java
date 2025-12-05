package com.ondra.users.exceptions;

import com.ondra.users.dto.ErrorDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

@Slf4j
public abstract class BaseExceptionHandler {

    protected ResponseEntity<ErrorDTO> createErrorResponse(
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
}