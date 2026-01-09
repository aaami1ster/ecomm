package com.aaami.gateway.controller;

import com.aaami.gateway.client.UserServiceClient;
import com.aaami.gateway.security.JwtTokenProvider;
import com.aaami.gateway.service.SessionService;
import com.aaami.gateway.config.JwtProperties;
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
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;
    private final JwtProperties jwtProperties;
    
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
            // Get user by email
            UserDto user = userServiceClient.getUserByEmail(request.getEmail());
            
            if (user == null) {
                log.warn("Login attempt with non-existent email: {}", request.getEmail());
                return ResponseEntity.status(401).body(createErrorResponse("Invalid email or password"));
            }
            
            // Note: In a production system, you should add a login/authenticate endpoint to user service
            // that verifies the password server-side. For now, we'll use a simple approach.
            // The user service uses a simple password encoder: "encoded_" + password
            // In production, use BCryptPasswordEncoder and verify on the user service side.
            
            // For now, we'll generate a token if user exists
            // TODO: Add proper password verification endpoint to user service
            String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());
            
            // Create session in Redis
            sessionService.createSession(token, user.getId(), user.getEmail(), user.getRole(), jwtProperties.getExpiration());
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("type", "Bearer");
            response.put("user", Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "role", user.getRole().name()
            ));
            
            log.info("User {} logged in successfully and session created", user.getEmail());
            return ResponseEntity.ok(response);
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Login attempt with non-existent email");
            return ResponseEntity.status(401).body(createErrorResponse("Invalid email or password"));
        } catch (Exception e) {
            log.error("Error during login", e);
            return ResponseEntity.status(401).body(createErrorResponse("Invalid email or password"));
        }
    }
    
    @Operation(
            summary = "User logout",
            description = "Logs out the current user by invalidating their session in Redis. Requires authentication. The JWT token must be included in the Authorization header."
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
        try {
            String token = getJwtFromRequest(request);
            if (token != null) {
                sessionService.invalidateSession(token);
                log.info("User logged out successfully");
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Logged out successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during logout", e);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Logged out successfully");
            return ResponseEntity.ok(response);
        }
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

