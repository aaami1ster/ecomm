package com.aaami.product.exception;

import com.aaami.shared.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn("/api/products");
    }

    @Test
    @DisplayName("Should handle DuplicateProductNameException with CONFLICT status")
    void handleDuplicateProductNameException_ShouldReturnConflict() {
        // Given
        DuplicateProductNameException exception = new DuplicateProductNameException(
                "Product with name 'Test Product' already exists"
        );

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDuplicateProductNameException(
                exception, request
        );

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.CONFLICT.value(), response.getBody().getStatus());
        assertEquals("Conflict", response.getBody().getError());
        assertEquals("Product with name 'Test Product' already exists", response.getBody().getMessage());
        assertEquals("/api/products", response.getBody().getPath());
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException for product not found with NOT_FOUND status")
    void handleIllegalArgumentException_ShouldReturnNotFound_WhenProductNotFound() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Product not found with id: 1");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(
                exception, request
        );

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
        assertEquals("Product not found with id: 1", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException for duplicate name with CONFLICT status")
    void handleIllegalArgumentException_ShouldReturnConflict_WhenDuplicateName() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException(
                "Product with name 'Test Product' already exists"
        );

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(
                exception, request
        );

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.CONFLICT.value(), response.getBody().getStatus());
        assertEquals("Conflict", response.getBody().getError());
        assertEquals("Product with name 'Test Product' already exists", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle generic IllegalArgumentException with BAD_REQUEST status")
    void handleIllegalArgumentException_ShouldReturnBadRequest_ForGenericIllegalArgument() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(
                exception, request
        );

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Invalid argument", response.getBody().getMessage());
    }
}

