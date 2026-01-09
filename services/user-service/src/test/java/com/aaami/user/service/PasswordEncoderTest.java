package com.aaami.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordEncoderTest {

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new PasswordEncoder();
    }

    @Test
    void encode_ShouldEncodePassword() {
        // Given
        String rawPassword = "password123";

        // When
        String encoded = passwordEncoder.encode(rawPassword);

        // Then
        assertNotNull(encoded);
        assertNotEquals(rawPassword, encoded);
        assertTrue(encoded.startsWith("encoded_"));
    }

    @Test
    void matches_ShouldReturnTrue_WhenPasswordMatches() {
        // Given
        String rawPassword = "password123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // When
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);

        // Then
        assertTrue(matches);
    }

    @Test
    void matches_ShouldReturnFalse_WhenPasswordDoesNotMatch() {
        // Given
        String rawPassword = "password123";
        String wrongPassword = "wrongPassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // When
        boolean matches = passwordEncoder.matches(wrongPassword, encodedPassword);

        // Then
        assertFalse(matches);
    }

    @Test
    void encode_ShouldHandleEmptyPassword() {
        // Given
        String rawPassword = "";

        // When
        String encoded = passwordEncoder.encode(rawPassword);

        // Then
        assertNotNull(encoded);
        assertEquals("encoded_", encoded);
    }

    @Test
    void matches_ShouldReturnFalse_WhenEncodedPasswordIsNull() {
        // Given
        String rawPassword = "password123";

        // When
        boolean matches = passwordEncoder.matches(rawPassword, null);

        // Then
        assertFalse(matches);
    }
}

