package com.aaami.gateway.exception;

import com.aaami.shared.exception.BaseGlobalExceptionHandler;
import com.aaami.shared.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;

/**
 * API Gateway exception handler.
 * Extends BaseGlobalExceptionHandler for common exception handling.
 * Adds gateway-specific exception handlers for downstream service communication.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends BaseGlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorResponse> handleHttpClientErrorException(
            HttpClientErrorException ex, HttpServletRequest request) {
        log.warn("HTTP client error: {} - {}", ex.getStatusCode(), ex.getMessage());
        
        try {
            // Try to parse the error response from the service
            ErrorResponse errorResponse = objectMapper.readValue(
                    ex.getResponseBodyAsString(), ErrorResponse.class);
            // Update the path to reflect the gateway path, not the service path
            errorResponse.setPath(request.getRequestURI());
            return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
        } catch (Exception e) {
            // If parsing fails, try to extract message from the response body or exception message
            String errorMessage = extractMessageFromResponse(ex.getResponseBodyAsString(), ex.getMessage());
            
            HttpStatus httpStatus = HttpStatus.resolve(ex.getStatusCode().value());
            String errorText = httpStatus != null ? httpStatus.getReasonPhrase() : "Error";
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(ex.getStatusCode().value())
                    .error(errorText)
                    .message(errorMessage)
                    .path(request.getRequestURI())
                    .build();
            return ResponseEntity.status(ex.getStatusCode()).body(error);
        }
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ErrorResponse> handleHttpServerErrorException(
            HttpServerErrorException ex, HttpServletRequest request) {
        log.error("HTTP server error: {} - {}", ex.getStatusCode(), ex.getMessage());
        
        try {
            // Try to parse the error response from the service
            ErrorResponse errorResponse = objectMapper.readValue(
                    ex.getResponseBodyAsString(), ErrorResponse.class);
            // Update the path to reflect the gateway path, not the service path
            errorResponse.setPath(request.getRequestURI());
            return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
        } catch (Exception e) {
            // If parsing fails, try to extract message from the response body or exception message
            String errorMessage = extractMessageFromResponse(ex.getResponseBodyAsString(), ex.getMessage());
            
            HttpStatus httpStatus = HttpStatus.resolve(ex.getStatusCode().value());
            String errorText = httpStatus != null ? httpStatus.getReasonPhrase() : "Error";
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(ex.getStatusCode().value())
                    .error(errorText)
                    .message(errorMessage)
                    .path(request.getRequestURI())
                    .build();
            return ResponseEntity.status(ex.getStatusCode()).body(error);
        }
    }

    private String extractMessageFromResponse(String responseBody, String fallbackMessage) {
        // First try to parse the response body as JSON
        if (responseBody != null && !responseBody.isEmpty()) {
            try {
                ErrorResponse errorResponse = objectMapper.readValue(responseBody, ErrorResponse.class);
                if (errorResponse.getMessage() != null && !errorResponse.getMessage().isEmpty()) {
                    return errorResponse.getMessage();
                }
            } catch (Exception ignored) {
                // If JSON parsing fails, try to extract message from string
            }
            
            // Try to extract message from JSON string using string manipulation
            if (responseBody.contains("\"message\"")) {
                try {
                    // Look for "message":"value" pattern
                    int messageStart = responseBody.indexOf("\"message\":\"") + 11;
                    if (messageStart > 10) {
                        int messageEnd = responseBody.indexOf("\"", messageStart);
                        if (messageEnd > messageStart) {
                            return responseBody.substring(messageStart, messageEnd);
                        }
                    }
                } catch (Exception ignored) {
                    // Fall through to fallback
                }
            }
        }
        
        // If response body parsing fails, try to extract from exception message
        if (fallbackMessage != null && fallbackMessage.contains("\"message\"")) {
            try {
                // Extract JSON from the exception message if it contains JSON
                int jsonStart = fallbackMessage.indexOf("{");
                int jsonEnd = fallbackMessage.lastIndexOf("}");
                if (jsonStart >= 0 && jsonEnd > jsonStart) {
                    String jsonString = fallbackMessage.substring(jsonStart, jsonEnd + 1);
                    ErrorResponse errorResponse = objectMapper.readValue(jsonString, ErrorResponse.class);
                    if (errorResponse.getMessage() != null && !errorResponse.getMessage().isEmpty()) {
                        return errorResponse.getMessage();
                    }
                }
                
                // Try string extraction as fallback
                int messageStart = fallbackMessage.indexOf("\"message\":\"") + 11;
                if (messageStart > 10) {
                    int messageEnd = fallbackMessage.indexOf("\"", messageStart);
                    if (messageEnd > messageStart) {
                        return fallbackMessage.substring(messageStart, messageEnd);
                    }
                }
            } catch (Exception ignored) {
                // Fall through to final fallback
            }
        }
        
        return fallbackMessage != null ? fallbackMessage : "An error occurred";
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ErrorResponse> handleRestClientException(
            RestClientException ex, HttpServletRequest request) {
        log.error("Rest client error: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_GATEWAY.value())
                .error("Bad Gateway")
                .message("Error communicating with service: " + ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("Method not supported: {} for {}", ex.getMethod(), request.getRequestURI());
        String supportedMethods = ex.getSupportedMethods() != null 
                ? String.join(", ", ex.getSupportedMethods()) 
                : "N/A";
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .error("Method Not Allowed")
                .message(String.format("Request method '%s' is not supported for this endpoint. Supported methods: %s", 
                        ex.getMethod(), supportedMethods))
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(error);
    }
}

