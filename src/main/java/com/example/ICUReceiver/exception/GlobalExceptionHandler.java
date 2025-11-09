package com.example.ICUReceiver.exception;

import com.example.ICUReceiver.dto.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure("Internal server error: " + e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException e) {
        String message = e
                .getBindingResult()
                .getAllErrors()
                .getFirst()
                .getDefaultMessage();
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure("Validation error: " + message));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFound(
            EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(e.getMessage()));
    }

    @ExceptionHandler(InvalidICUSignalException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidSignal(
            InvalidICUSignalException e) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure(e.getMessage()));
    }
}
