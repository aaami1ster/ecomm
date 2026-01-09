package com.aaami.order.service;

import com.aaami.shared.dto.OrderDto;
import com.aaami.shared.dto.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IdempotencyService Tests")
class IdempotencyServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private IdempotencyService idempotencyService;

    private OrderDto orderDto;
    private String idempotencyKey;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        idempotencyService = new IdempotencyService(redisTemplate);
        // Use reflection to set expirationHours for testing
        try {
            java.lang.reflect.Field field = IdempotencyService.class.getDeclaredField("expirationHours");
            field.setAccessible(true);
            field.set(idempotencyService, 24L);
        } catch (Exception e) {
            // Ignore reflection errors in tests
        }
        
        idempotencyKey = "test-key-123";
        orderDto = OrderDto.builder()
                .id(1L)
                .userId(1L)
                .status(OrderStatus.CONFIRMED)
                .orderTotal(new BigDecimal("100.00"))
                .build();
    }

    @Test
    @DisplayName("Should return cached order when idempotency key exists")
    void getCachedOrder_ShouldReturnOrder_WhenKeyExists() {
        // Given
        when(valueOperations.get("idempotency:order:" + idempotencyKey)).thenReturn(orderDto);

        // When
        OrderDto result = idempotencyService.getCachedOrder(idempotencyKey);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
        verify(valueOperations).get("idempotency:order:" + idempotencyKey);
    }

    @Test
    @DisplayName("Should return null when idempotency key does not exist")
    void getCachedOrder_ShouldReturnNull_WhenKeyNotFound() {
        // Given
        when(valueOperations.get("idempotency:order:" + idempotencyKey)).thenReturn(null);

        // When
        OrderDto result = idempotencyService.getCachedOrder(idempotencyKey);

        // Then
        assertNull(result);
        verify(valueOperations).get("idempotency:order:" + idempotencyKey);
    }

    @Test
    @DisplayName("Should return null when idempotency key is null")
    void getCachedOrder_ShouldReturnNull_WhenKeyIsNull() {
        // When
        OrderDto result = idempotencyService.getCachedOrder(null);

        // Then
        assertNull(result);
        verify(valueOperations, never()).get(anyString());
    }

    @Test
    @DisplayName("Should cache order with expiration")
    void cacheOrder_ShouldStoreOrderInRedis() {
        // When
        idempotencyService.cacheOrder(idempotencyKey, orderDto);

        // Then
        verify(valueOperations).set(
                eq("idempotency:order:" + idempotencyKey),
                eq(orderDto),
                eq(24L),
                eq(TimeUnit.HOURS)
        );
    }

    @Test
    @DisplayName("Should not cache when idempotency key is null")
    void cacheOrder_ShouldNotStore_WhenKeyIsNull() {
        // When
        idempotencyService.cacheOrder(null, orderDto);

        // Then
        verify(valueOperations, never()).set(anyString(), any(), anyLong(), any(TimeUnit.class));
    }

    @Test
    @DisplayName("Should check if key exists")
    void keyExists_ShouldReturnTrue_WhenKeyExists() {
        // Given
        when(redisTemplate.hasKey("idempotency:order:" + idempotencyKey)).thenReturn(true);

        // When
        boolean exists = idempotencyService.keyExists(idempotencyKey);

        // Then
        assertTrue(exists);
        verify(redisTemplate).hasKey("idempotency:order:" + idempotencyKey);
    }

    @Test
    @DisplayName("Should return false when key does not exist")
    void keyExists_ShouldReturnFalse_WhenKeyNotFound() {
        // Given
        when(redisTemplate.hasKey("idempotency:order:" + idempotencyKey)).thenReturn(false);

        // When
        boolean exists = idempotencyService.keyExists(idempotencyKey);

        // Then
        assertFalse(exists);
        verify(redisTemplate).hasKey("idempotency:order:" + idempotencyKey);
    }
}

