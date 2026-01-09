package com.aaami.gateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ServiceProperties Tests")
class ServicePropertiesTest {

    private ServiceProperties serviceProperties;

    @BeforeEach
    void setUp() {
        serviceProperties = new ServiceProperties();
    }

    @Test
    @DisplayName("Should set and get user service URL")
    void setAndGetUserServiceUrl_ShouldWork() {
        // Given
        String url = "http://localhost:8083";

        // When
        serviceProperties.setUserServiceUrl(url);

        // Then
        assertEquals(url, serviceProperties.getUserServiceUrl());
    }

    @Test
    @DisplayName("Should set and get product service URL")
    void setAndGetProductServiceUrl_ShouldWork() {
        // Given
        String url = "http://localhost:8081";

        // When
        serviceProperties.setProductServiceUrl(url);

        // Then
        assertEquals(url, serviceProperties.getProductServiceUrl());
    }

    @Test
    @DisplayName("Should set and get order service URL")
    void setAndGetOrderServiceUrl_ShouldWork() {
        // Given
        String url = "http://localhost:8082";

        // When
        serviceProperties.setOrderServiceUrl(url);

        // Then
        assertEquals(url, serviceProperties.getOrderServiceUrl());
    }
}

