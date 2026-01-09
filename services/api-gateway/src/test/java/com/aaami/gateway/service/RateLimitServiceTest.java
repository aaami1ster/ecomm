package com.aaami.gateway.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitService Tests")
class RateLimitServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        ReflectionTestUtils.setField(rateLimitService, "maxRequests", 5);
        ReflectionTestUtils.setField(rateLimitService, "windowMinutes", 15);
    }

    @Test
    @DisplayName("Should allow request when under limit")
    void isAllowed_ShouldReturnTrue_WhenUnderLimit() {
        // Given
        String key = "192.168.1.1";
        when(valueOperations.increment("ratelimit:192.168.1.1")).thenReturn(1L);
        when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        // When
        boolean result = rateLimitService.isAllowed(key);

        // Then
        assertTrue(result);
        verify(valueOperations).increment("ratelimit:192.168.1.1");
        verify(redisTemplate).expire(eq("ratelimit:192.168.1.1"), eq(15L), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("Should allow request when at limit")
    void isAllowed_ShouldReturnTrue_WhenAtLimit() {
        // Given
        String key = "192.168.1.1";
        when(valueOperations.increment("ratelimit:192.168.1.1")).thenReturn(5L);

        // When
        boolean result = rateLimitService.isAllowed(key);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Should deny request when over limit")
    void isAllowed_ShouldReturnFalse_WhenOverLimit() {
        // Given
        String key = "192.168.1.1";
        when(valueOperations.increment("ratelimit:192.168.1.1")).thenReturn(6L);

        // When
        boolean result = rateLimitService.isAllowed(key);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should allow request when Redis error occurs (fail open)")
    void isAllowed_ShouldReturnTrue_WhenRedisError() {
        // Given
        String key = "192.168.1.1";
        when(valueOperations.increment(anyString())).thenThrow(new RuntimeException("Redis error"));

        // When
        boolean result = rateLimitService.isAllowed(key);

        // Then
        assertTrue(result); // Fail open
    }

    @Test
    @DisplayName("Should use custom limits when provided")
    void isAllowed_ShouldUseCustomLimits_WhenProvided() {
        // Given
        String key = "192.168.1.1";
        int maxRequests = 10;
        int windowMinutes = 30;
        when(valueOperations.increment("ratelimit:192.168.1.1")).thenReturn(1L);
        when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        // When
        boolean result = rateLimitService.isAllowed(key, maxRequests, windowMinutes);

        // Then
        assertTrue(result);
        verify(redisTemplate).expire(eq("ratelimit:192.168.1.1"), eq(30L), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("Should not set expiration when count is not 1")
    void isAllowed_ShouldNotSetExpiration_WhenCountIsNotOne() {
        // Given
        String key = "192.168.1.1";
        when(valueOperations.increment("ratelimit:192.168.1.1")).thenReturn(2L);

        // When
        rateLimitService.isAllowed(key);

        // Then
        verify(redisTemplate, never()).expire(anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    @DisplayName("Should return remaining requests correctly")
    void getRemainingRequests_ShouldReturnCorrectRemaining() {
        // Given
        String key = "192.168.1.1";
        when(valueOperations.get("ratelimit:192.168.1.1")).thenReturn(2);

        // When
        int remaining = rateLimitService.getRemainingRequests(key);

        // Then
        assertEquals(3, remaining); // 5 - 2 = 3
    }

    @Test
    @DisplayName("Should return max requests when key doesn't exist")
    void getRemainingRequests_ShouldReturnMaxRequests_WhenKeyDoesNotExist() {
        // Given
        String key = "192.168.1.1";
        when(valueOperations.get("ratelimit:192.168.1.1")).thenReturn(null);

        // When
        int remaining = rateLimitService.getRemainingRequests(key);

        // Then
        assertEquals(5, remaining);
    }

    @Test
    @DisplayName("Should return max requests when Redis error occurs")
    void getRemainingRequests_ShouldReturnMaxRequests_WhenRedisError() {
        // Given
        String key = "192.168.1.1";
        when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis error"));

        // When
        int remaining = rateLimitService.getRemainingRequests(key);

        // Then
        assertEquals(5, remaining);
    }

    @Test
    @DisplayName("Should return current count correctly")
    void getCurrentCount_ShouldReturnCorrectCount() {
        // Given
        String key = "192.168.1.1";
        when(valueOperations.get("ratelimit:192.168.1.1")).thenReturn(3);

        // When
        int count = rateLimitService.getCurrentCount(key);

        // Then
        assertEquals(3, count);
    }

    @Test
    @DisplayName("Should return zero when key doesn't exist")
    void getCurrentCount_ShouldReturnZero_WhenKeyDoesNotExist() {
        // Given
        String key = "192.168.1.1";
        when(valueOperations.get("ratelimit:192.168.1.1")).thenReturn(null);

        // When
        int count = rateLimitService.getCurrentCount(key);

        // Then
        assertEquals(0, count);
    }

    @Test
    @DisplayName("Should return zero when Redis error occurs")
    void getCurrentCount_ShouldReturnZero_WhenRedisError() {
        // Given
        String key = "192.168.1.1";
        when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis error"));

        // When
        int count = rateLimitService.getCurrentCount(key);

        // Then
        assertEquals(0, count);
    }

    @Test
    @DisplayName("Should reset rate limit for a key")
    void resetRateLimit_ShouldDeleteKey() {
        // Given
        String key = "192.168.1.1";

        // When
        rateLimitService.resetRateLimit(key);

        // Then
        verify(redisTemplate).delete("ratelimit:192.168.1.1");
    }

    @Test
    @DisplayName("Should handle exception gracefully when reset fails")
    void resetRateLimit_ShouldHandleException_WhenDeletionFails() {
        // Given
        String key = "192.168.1.1";
        doThrow(new RuntimeException("Redis error")).when(redisTemplate).delete(anyString());

        // When & Then - should not throw
        assertDoesNotThrow(() -> rateLimitService.resetRateLimit(key));
    }

    @Test
    @DisplayName("Should return max requests and window minutes")
    void getMaxRequestsAndWindowMinutes_ShouldReturnConfiguredValues() {
        // When
        int maxRequests = rateLimitService.getMaxRequests();
        int windowMinutes = rateLimitService.getWindowMinutes();

        // Then
        assertEquals(5, maxRequests);
        assertEquals(15, windowMinutes);
    }
}

