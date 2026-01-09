package com.aaami.gateway.security;

import com.aaami.gateway.config.JwtProperties;
import com.aaami.shared.dto.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret("test-secret-key-that-is-long-enough-for-hmac-sha256-algorithm");
        jwtProperties.setExpiration(86400000L); // 24 hours
        tokenProvider = new JwtTokenProvider(jwtProperties);
    }

    @Test
    void generateToken_ShouldGenerateValidToken() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        UserRole role = UserRole.USER;

        // When
        String token = tokenProvider.generateToken(userId, email, role);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void validateToken_ShouldReturnTrue_ForValidToken() {
        // Given
        String token = tokenProvider.generateToken(1L, "test@example.com", UserRole.USER);

        // When
        boolean isValid = tokenProvider.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void getUserIdFromToken_ShouldExtractUserId() {
        // Given
        Long expectedUserId = 1L;
        String token = tokenProvider.generateToken(expectedUserId, "test@example.com", UserRole.USER);

        // When
        Long userId = tokenProvider.getUserIdFromToken(token);

        // Then
        assertEquals(expectedUserId, userId);
    }

    @Test
    void getEmailFromToken_ShouldExtractEmail() {
        // Given
        String expectedEmail = "test@example.com";
        String token = tokenProvider.generateToken(1L, expectedEmail, UserRole.USER);

        // When
        String email = tokenProvider.getEmailFromToken(token);

        // Then
        assertEquals(expectedEmail, email);
    }

    @Test
    void getRoleFromToken_ShouldExtractRole() {
        // Given
        UserRole expectedRole = UserRole.ADMIN;
        String token = tokenProvider.generateToken(1L, "test@example.com", expectedRole);

        // When
        UserRole role = tokenProvider.getRoleFromToken(token);

        // Then
        assertEquals(expectedRole, role);
    }

    @Test
    void validateToken_ShouldReturnFalse_ForInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = tokenProvider.validateToken(invalidToken);

        // Then
        assertFalse(isValid);
    }
}

