package com.aaami.gateway.controller;

import com.aaami.gateway.client.UserServiceClient;
import com.aaami.gateway.security.JwtTokenProvider;
import com.aaami.shared.dto.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints for user login and logout")
public class AuthController {
    
    private final UserServiceClient userServiceClient;
    private final JwtTokenProvider tokenProvider;
    
    @Operation(
            summary = "User login",
            description = "Authenticates a user and returns a JWT token. The token should be included in subsequent requests in the Authorization header as 'Bearer {token}'."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class),
                            examples = @ExampleObject(
                                    value = "{\n" +
                                            "  \"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\",\n" +
                                            "  \"type\": \"Bearer\",\n" +
                                            "  \"user\": {\n" +
                                            "    \"id\": 1,\n" +
                                            "    \"email\": \"user@example.com\",\n" +
                                            "    \"role\": \"USER\"\n" +
                                            "  }\n" +
                                            "}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid email or password",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\n" +
                                            "  \"error\": \"Invalid email or password\"\n" +
                                            "}"
                            )
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        try {
            // Verify password with user service (returns user if valid, throws exception if invalid)
            UserDto user = userServiceClient.verifyPassword(request.getEmail(), request.getPassword());
            
            if (user == null) {
                log.warn("Login attempt failed: User not found for email: {}", request.getEmail());
                return ResponseEntity.status(401).body(createErrorResponse("Invalid email or password"));
            }
            
            // Generate JWT token for authenticated user
            String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("type", "Bearer");
            response.put("user", Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "role", user.getRole().name()
            ));
            
            log.info("User {} logged in successfully", user.getEmail());
            return ResponseEntity.ok(response);
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Login attempt with non-existent email: {}", request.getEmail());
            return ResponseEntity.status(401).body(createErrorResponse("Invalid email or password"));
        } catch (HttpClientErrorException.BadRequest e) {
            log.warn("Login attempt with invalid password for email: {}", request.getEmail());
            return ResponseEntity.status(401).body(createErrorResponse("Invalid email or password"));
        } catch (Exception e) {
            log.error("Error during login for email: {}", request.getEmail(), e);
            return ResponseEntity.status(401).body(createErrorResponse("Invalid email or password"));
        }
    }
    
    @Operation(
            summary = "User logout",
            description = "Logs out the current user. In a stateless JWT system, logout is handled client-side by discarding the token. This endpoint exists for API consistency. Requires authentication. The JWT token must be included in the Authorization header."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Logout successful",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\n" +
                                            "  \"message\": \"Logged out successfully\"\n" +
                                            "}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or invalid JWT token",
                    content = @Content
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        // In a stateless JWT system, logout is handled client-side by discarding the token
        // This endpoint exists for API consistency and returns success
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        log.info("Logout request received (stateless JWT - client should discard token)");
        return ResponseEntity.ok(response);
    }
    
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
    
    @Schema(description = "Login request payload")
    @Data
    public static class LoginRequest {
        @Schema(description = "User email address", example = "user@example.com", required = true)
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        private String email;
        
        @Schema(description = "User password", example = "password123", required = true)
        @NotBlank(message = "Password is required")
        private String password;
    }
    
    @Schema(description = "Login response containing JWT token and user information")
    public static class LoginResponse {
        @Schema(description = "JWT authentication token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        private String token;
        
        @Schema(description = "Token type", example = "Bearer")
        private String type;
        
        @Schema(description = "User information")
        private UserInfo user;
        
        @Schema(description = "User information in login response")
        public static class UserInfo {
            @Schema(description = "User ID", example = "1")
            private Long id;
            
            @Schema(description = "User email", example = "user@example.com")
            private String email;
            
            @Schema(description = "User role", example = "USER", allowableValues = {"USER", "PREMIUM_USER", "ADMIN"})
            private String role;
        }
    }
}

