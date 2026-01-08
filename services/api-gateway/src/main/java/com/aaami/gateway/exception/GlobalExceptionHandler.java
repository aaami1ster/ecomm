package com.aaami.gateway.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation error: {}", ex.getMessage());
        StringBuilder errorMessage = new StringBuilder("Validation failed: ");
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = error instanceof org.springframework.validation.FieldError 
                    ? ((org.springframework.validation.FieldError) error).getField() 
                    : error.getObjectName();
            String message = error.getDefaultMessage();
            errorMessage.append(fieldName).append(" - ").append(message).append("; ");
        });
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(errorMessage.toString())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Invalid request body: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message("Invalid request body: " + ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
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

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.warn("Type mismatch error: {}", ex.getMessage());
        
        String errorMessage = ex.getMessage();
        if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {
            // Provide helpful message for enum type mismatches
            Object[] enumConstants = ex.getRequiredType().getEnumConstants();
            String validValues = Arrays.stream(enumConstants)
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            errorMessage = String.format("Invalid value '%s' for parameter '%s'. Valid values are: %s",
                    ex.getValue(), ex.getName(), validValues);
        }
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(errorMessage)
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error: ", ex);
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred")
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

