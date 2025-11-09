package com.example.ICUReceiver.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ApiResponseTest {

    @Test
    void testSuccessResponse() {
        String message = "Operation completed";
        String data = "SampleData";

        ApiResponse<String> response = ApiResponse.success(message, data);

        assertTrue(response.isSuccess());
        assertEquals(message, response.getMessage());
        assertEquals(data, response.getData());
    }

    @Test
    void testFailureResponse() {
        String message = "Operation failed";

        ApiResponse<String> response = ApiResponse.failure(message);

        assertFalse(response.isSuccess());
        assertEquals(message, response.getMessage());
        assertNull(response.getData());
    }
}
