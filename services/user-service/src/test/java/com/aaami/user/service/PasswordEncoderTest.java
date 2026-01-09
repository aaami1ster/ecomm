package com.aaami.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PasswordEncoder Tests")
class PasswordEncoderTest {

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new PasswordEncoder();
    }

    @Test
    @DisplayName("Should encode password using BCrypt")
    void encode_ShouldEncodePassword() {
        // Given
        String rawPassword = "Password123";

        // When
        String encoded = passwordEncoder.encode(rawPassword);

        // Then
        assertNotNull(encoded);
        assertNotEquals(rawPassword, encoded);
        // BCrypt hashes start with $2a$, $2b$, or $2y$
        assertTrue(encoded.startsWith("$2"));
        // BCrypt hashes are 60 characters long
        assertEquals(60, encoded.length());
    }

    @Test
    @DisplayName("Should return true when password matches encoded password")
    void matches_ShouldReturnTrue_WhenPasswordMatches() {
        // Given
        String rawPassword = "Password123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // When
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);

        // Then
        assertTrue(matches);
    }

    @Test
    @DisplayName("Should return false when password does not match")
    void matches_ShouldReturnFalse_WhenPasswordDoesNotMatch() {
        // Given
        String rawPassword = "Password123";
        String wrongPassword = "WrongPassword456";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // When
        boolean matches = passwordEncoder.matches(wrongPassword, encodedPassword);

        // Then
        assertFalse(matches);
    }

    @Test
    @DisplayName("Should encode empty password")
    void encode_ShouldHandleEmptyPassword() {
        // Given
        String rawPassword = "";

        // When
        String encoded = passwordEncoder.encode(rawPassword);

        // Then
        assertNotNull(encoded);
        assertTrue(encoded.startsWith("$2"));
        assertEquals(60, encoded.length());
    }

    @Test
    @DisplayName("Should return false when encoded password is null")
    void matches_ShouldReturnFalse_WhenEncodedPasswordIsNull() {
        // Given
        String rawPassword = "Password123";

        // When
        boolean matches = passwordEncoder.matches(rawPassword, null);

        // Then
        assertFalse(matches);
    }

    @Test
    @DisplayName("Should return false when raw password is null")
    void matches_ShouldReturnFalse_WhenRawPasswordIsNull() {
        // Given
        String encodedPassword = passwordEncoder.encode("Password123");

        // When
        boolean matches = passwordEncoder.matches(null, encodedPassword);

        // Then
        assertFalse(matches);
    }

    @Test
    @DisplayName("Should generate different hashes for same password (salt variation)")
    void encode_ShouldGenerateDifferentHashes_ForSamePassword() {
        // Given
        String rawPassword = "Password123";

        // When
        String encoded1 = passwordEncoder.encode(rawPassword);
        String encoded2 = passwordEncoder.encode(rawPassword);

        // Then
        assertNotEquals(encoded1, encoded2); // Different salts = different hashes
        // But both should match the original password
        assertTrue(passwordEncoder.matches(rawPassword, encoded1));
        assertTrue(passwordEncoder.matches(rawPassword, encoded2));
    }

    @Test
    @DisplayName("Should throw exception when encoding null password")
    void encode_ShouldThrowException_WhenPasswordIsNull() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> passwordEncoder.encode(null));
    }
}

