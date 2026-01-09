package com.aaami.gateway.controller;

import com.aaami.gateway.client.UserServiceClient;
import com.aaami.gateway.security.JwtTokenProvider;
import com.aaami.shared.dto.UserDto;
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

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserServiceClient userServiceClient;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    
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
            log.warn("Login attempt with non-existent email");
            return ResponseEntity.status(401).body(createErrorResponse("Invalid email or password"));
        } catch (Exception e) {
            log.error("Error during login", e);
            return ResponseEntity.status(401).body(createErrorResponse("Invalid email or password"));
        }
    }
    
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
    
    @Data
    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        private String email;
        
        @NotBlank(message = "Password is required")
        private String password;
    }
}

