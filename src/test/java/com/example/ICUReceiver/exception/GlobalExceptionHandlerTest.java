package com.example.ICUReceiver.exception;

import com.example.ICUReceiver.dto.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleGenericException() {
        Exception ex = new Exception("Something went wrong");

        ResponseEntity<ApiResponse<Void>> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Something went wrong"));
    }

    @Test
    void testHandleEntityNotFound() {
        EntityNotFoundException ex = new EntityNotFoundException("Not found");

        ResponseEntity<ApiResponse<Void>> response = handler.handleEntityNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Not found", response.getBody().getMessage());
    }

    @Test
    void testHandleInvalidSignal() {
        InvalidICUSignalException ex = new InvalidICUSignalException("Invalid signal");

        ResponseEntity<ApiResponse<Void>> response = handler.handleInvalidSignal(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid signal", response.getBody().getMessage());
    }
}
