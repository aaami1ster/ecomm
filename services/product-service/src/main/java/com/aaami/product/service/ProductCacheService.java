package com.aaami.product.service;

import com.aaami.shared.dto.ProductDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service for caching product data in Redis using cache-aside pattern.
 * 
 * Cache-aside pattern:
 * 1. Check cache first
 * 2. If cache miss, load from database
 * 3. Store in cache for future requests
 * 4. Invalidate cache on updates/deletes
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCacheService {
    
    private static final String CACHE_KEY_PREFIX = "product:";
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Value("${product.cache.ttl-minutes:30}")
    private long cacheTtlMinutes;
    
    /**
     * Gets a product from cache by ID.
     * 
     * @param productId The product ID
     * @return ProductDto if found in cache, null otherwise
     */
    public ProductDto getCachedProduct(Long productId) {
        String cacheKey = buildCacheKey(productId);
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof ProductDto) {
                log.debug("Cache hit for product ID: {}", productId);
                return (ProductDto) cached;
            }
        } catch (Exception e) {
            log.error("Error retrieving product from cache: {}", productId, e);
        }
        log.debug("Cache miss for product ID: {}", productId);
        return null;
    }
    
    /**
     * Caches a product with TTL.
     * 
     * @param product The product to cache
     */
    public void cacheProduct(ProductDto product) {
        if (product == null || product.getId() == null) {
            return;
        }
        
        String cacheKey = buildCacheKey(product.getId());
        try {
            redisTemplate.opsForValue().set(cacheKey, product, cacheTtlMinutes, TimeUnit.MINUTES);
            log.debug("Cached product ID: {} with TTL: {} minutes", product.getId(), cacheTtlMinutes);
        } catch (Exception e) {
            log.error("Error caching product: {}", product.getId(), e);
            // Don't throw - caching is best-effort, should not break the flow
        }
    }
    
    /**
     * Invalidates (deletes) a product from cache.
     * 
     * @param productId The product ID to invalidate
     */
    public void invalidateProduct(Long productId) {
        String cacheKey = buildCacheKey(productId);
        try {
            redisTemplate.delete(cacheKey);
            log.debug("Invalidated cache for product ID: {}", productId);
        } catch (Exception e) {
            log.error("Error invalidating product cache: {}", productId, e);
            // Don't throw - cache invalidation is best-effort
        }
    }
    
    /**
     * Invalidates all product caches (use with caution).
     * This is useful for bulk operations or cache warming scenarios.
     */
    public void invalidateAllProducts() {
        try {
            // Use pattern matching to find all product keys
            var keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Invalidated {} product cache entries", keys.size());
            }
        } catch (Exception e) {
            log.error("Error invalidating all product caches", e);
        }
    }
    
    private String buildCacheKey(Long productId) {
        return CACHE_KEY_PREFIX + productId;
    }
}

