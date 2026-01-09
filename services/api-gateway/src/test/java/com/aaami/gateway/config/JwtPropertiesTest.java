package com.aaami.gateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtProperties Tests")
class JwtPropertiesTest {

    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
    }

    @Test
    @DisplayName("Should set and get secret")
    void setAndGetSecret_ShouldWork() {
        // Given
        String secret = "test-secret";

        // When
        jwtProperties.setSecret(secret);

        // Then
        assertEquals(secret, jwtProperties.getSecret());
    }

    @Test
    @DisplayName("Should set and get expiration")
    void setAndGetExpiration_ShouldWork() {
        // Given
        Long expiration = 86400000L;

        // When
        jwtProperties.setExpiration(expiration);

        // Then
        assertEquals(expiration, jwtProperties.getExpiration());
    }
}

