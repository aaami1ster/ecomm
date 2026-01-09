package com.aaami.product.service;

import com.aaami.shared.dto.ProductDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductCacheService Tests")
class ProductCacheServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private Cursor<String> cursor;

    @InjectMocks
    private ProductCacheService cacheService;

    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        ReflectionTestUtils.setField(cacheService, "cacheTtlMinutes", 30L);
    }

    @Test
    @DisplayName("Should return cached product when it exists in cache")
    void getCachedProduct_ShouldReturnProduct_WhenCacheHit() {
        // Given
        Long productId = 1L;
        productDto = ProductDto.builder()
                .id(productId)
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .quantity(10)
                .build();
        
        when(valueOperations.get("product:1")).thenReturn(productDto);

        // When
        ProductDto result = cacheService.getCachedProduct(productId);

        // Then
        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals("Test Product", result.getName());
        verify(valueOperations).get("product:1");
    }

    @Test
    @DisplayName("Should return null when product not in cache")
    void getCachedProduct_ShouldReturnNull_WhenCacheMiss() {
        // Given
        Long productId = 1L;
        when(valueOperations.get("product:1")).thenReturn(null);

        // When
        ProductDto result = cacheService.getCachedProduct(productId);

        // Then
        assertNull(result);
        verify(valueOperations).get("product:1");
    }

    @Test
    @DisplayName("Should return null when cached value is not ProductDto")
    void getCachedProduct_ShouldReturnNull_WhenCachedValueIsNotProductDto() {
        // Given
        Long productId = 1L;
        when(valueOperations.get("product:1")).thenReturn("invalid");

        // When
        ProductDto result = cacheService.getCachedProduct(productId);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle exception gracefully when cache retrieval fails")
    void getCachedProduct_ShouldReturnNull_WhenExceptionOccurs() {
        // Given
        Long productId = 1L;
        when(valueOperations.get("product:1")).thenThrow(new RuntimeException("Redis error"));

        // When
        ProductDto result = cacheService.getCachedProduct(productId);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should cache product with TTL")
    void cacheProduct_ShouldCacheProduct_WhenProductIsValid() {
        // Given
        productDto = ProductDto.builder()
                .id(1L)
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .quantity(10)
                .build();

        // When
        cacheService.cacheProduct(productDto);

        // Then
        verify(valueOperations).set(eq("product:1"), eq(productDto), eq(30L), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("Should not cache when product is null")
    void cacheProduct_ShouldNotCache_WhenProductIsNull() {
        // When
        cacheService.cacheProduct(null);

        // Then
        verify(valueOperations, never()).set(anyString(), any(), anyLong(), any(TimeUnit.class));
    }

    @Test
    @DisplayName("Should not cache when product ID is null")
    void cacheProduct_ShouldNotCache_WhenProductIdIsNull() {
        // Given
        ProductDto productWithoutId = ProductDto.builder()
                .name("Test Product")
                .build();

        // When
        cacheService.cacheProduct(productWithoutId);

        // Then
        verify(valueOperations, never()).set(anyString(), any(), anyLong(), any(TimeUnit.class));
    }

    @Test
    @DisplayName("Should handle exception gracefully when caching fails")
    void cacheProduct_ShouldHandleException_WhenCachingFails() {
        // Given
        productDto = ProductDto.builder()
                .id(1L)
                .name("Test Product")
                .build();
        doThrow(new RuntimeException("Redis error")).when(valueOperations)
                .set(anyString(), any(), anyLong(), any(TimeUnit.class));

        // When & Then - should not throw
        assertDoesNotThrow(() -> cacheService.cacheProduct(productDto));
    }

    @Test
    @DisplayName("Should invalidate product from cache")
    void invalidateProduct_ShouldDeleteFromCache() {
        // Given
        Long productId = 1L;

        // When
        cacheService.invalidateProduct(productId);

        // Then
        verify(redisTemplate).delete("product:1");
    }

    @Test
    @DisplayName("Should handle exception gracefully when invalidation fails")
    void invalidateProduct_ShouldHandleException_WhenDeletionFails() {
        // Given
        Long productId = 1L;
        doThrow(new RuntimeException("Redis error")).when(redisTemplate).delete(anyString());

        // When & Then - should not throw
        assertDoesNotThrow(() -> cacheService.invalidateProduct(productId));
    }

    @Test
    @DisplayName("Should invalidate all products using SCAN")
    void invalidateAllProducts_ShouldDeleteAllMatchingKeys() {
        // Given
        Set<String> keys = new HashSet<>();
        keys.add("product:1");
        keys.add("product:2");
        keys.add("product:3");

        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, true, true, false);
        when(cursor.next()).thenReturn("product:1", "product:2", "product:3");

        // When
        cacheService.invalidateAllProducts();

        // Then
        verify(redisTemplate).delete(keys);
    }

    @Test
    @DisplayName("Should handle empty cache when invalidating all")
    void invalidateAllProducts_ShouldHandleEmptyCache() {
        // Given
        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(false);

        // When
        cacheService.invalidateAllProducts();

        // Then
        verify(redisTemplate, never()).delete(any(Set.class));
    }

    @Test
    @DisplayName("Should handle exception gracefully when invalidating all fails")
    void invalidateAllProducts_ShouldHandleException_WhenScanFails() {
        // Given
        when(redisTemplate.scan(any(ScanOptions.class))).thenThrow(new RuntimeException("Redis error"));

        // When & Then - should not throw
        assertDoesNotThrow(() -> cacheService.invalidateAllProducts());
    }
}

