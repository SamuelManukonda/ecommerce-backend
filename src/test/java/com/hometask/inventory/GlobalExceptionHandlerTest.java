package com.hometask.inventory;

import com.hometask.inventory.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleIllegalArgumentException_ShouldReturnBadRequest() {
        // Given
        String errorMessage = "Invalid argument provided";
        IllegalArgumentException exception = new IllegalArgumentException(errorMessage);

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleIllegalArgumentException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.get("status"));
        assertEquals("Bad Request", body.get("error"));
        assertEquals(errorMessage, body.get("message"));
        assertNotNull(body.get("timestamp"));
        assertTrue(body.get("timestamp") instanceof LocalDateTime);
    }

    @Test
    void handleIllegalArgumentException_WithNullMessage_ShouldReturnBadRequest() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException((String) null);

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleIllegalArgumentException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.get("status"));
        assertEquals("Bad Request", body.get("error"));
        assertNull(body.get("message"));
    }

    @Test
    void handleGeneralException_ShouldReturnInternalServerError() {
        // Given
        Exception exception = new Exception("Some unexpected error");

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleGeneralException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), body.get("status"));
        assertEquals("Internal Server Error", body.get("error"));
        assertEquals("An unexpected error occurred", body.get("message"));
        assertNotNull(body.get("timestamp"));
        assertTrue(body.get("timestamp") instanceof LocalDateTime);
    }

    @Test
    void handleGeneralException_WithRuntimeException_ShouldReturnInternalServerError() {
        // Given
        RuntimeException exception = new RuntimeException("Runtime error occurred");

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleGeneralException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), body.get("status"));
        assertEquals("Internal Server Error", body.get("error"));
        assertEquals("An unexpected error occurred", body.get("message"));
    }

    @Test
    void handleGeneralException_WithNullPointerException_ShouldReturnInternalServerError() {
        // Given
        NullPointerException exception = new NullPointerException("Null pointer error");

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleGeneralException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), body.get("status"));
        assertEquals("Internal Server Error", body.get("error"));
        // The message should always be generic for security reasons
        assertEquals("An unexpected error occurred", body.get("message"));
    }

    @Test
    void handleIllegalArgumentException_ResponseBodyContainsFourFields() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Test error");

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleIllegalArgumentException(exception);

        // Then
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(4, body.size());
        assertTrue(body.containsKey("timestamp"));
        assertTrue(body.containsKey("status"));
        assertTrue(body.containsKey("error"));
        assertTrue(body.containsKey("message"));
    }

    @Test
    void handleGeneralException_ResponseBodyContainsFourFields() {
        // Given
        Exception exception = new Exception("Test error");

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleGeneralException(exception);

        // Then
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(4, body.size());
        assertTrue(body.containsKey("timestamp"));
        assertTrue(body.containsKey("status"));
        assertTrue(body.containsKey("error"));
        assertTrue(body.containsKey("message"));
    }
}

