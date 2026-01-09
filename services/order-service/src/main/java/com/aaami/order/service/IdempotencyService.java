package com.aaami.order.service;

import com.aaami.shared.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {
    
    private static final String IDEMPOTENCY_KEY_PREFIX = "idempotency:order:";
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Value("${idempotency.key.expiration-hours:24}")
    private long expirationHours;
    
    /**
     * Checks if an idempotency key exists and returns the cached order if found.
     * 
     * @param idempotencyKey The idempotency key to check
     * @return The cached OrderDto if the key exists, null otherwise
     */
    public OrderDto getCachedOrder(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isEmpty()) {
            return null;
        }
        
        String key = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof OrderDto) {
                log.debug("Found cached order for idempotency key: {}", idempotencyKey);
                return (OrderDto) cached;
            }
        } catch (Exception e) {
            log.error("Error retrieving idempotency key from Redis: {}", idempotencyKey, e);
        }
        return null;
    }
    
    /**
     * Stores an order with its idempotency key in Redis.
     * 
     * @param idempotencyKey The idempotency key
     * @param order The order to cache
     */
    public void cacheOrder(String idempotencyKey, OrderDto order) {
        if (idempotencyKey == null || idempotencyKey.isEmpty()) {
            return;
        }
        
        String key = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
        try {
            redisTemplate.opsForValue().set(key, order, expirationHours, TimeUnit.HOURS);
            log.debug("Cached order for idempotency key: {} with expiration: {} hours", idempotencyKey, expirationHours);
        } catch (Exception e) {
            log.error("Error caching idempotency key in Redis: {}", idempotencyKey, e);
            // Don't throw exception - idempotency is best-effort, order creation should still succeed
        }
    }
    
    /**
     * Checks if an idempotency key already exists (without retrieving the value).
     * This is useful for checking if a key exists before processing.
     * 
     * @param idempotencyKey The idempotency key to check
     * @return true if the key exists, false otherwise
     */
    public boolean keyExists(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isEmpty()) {
            return false;
        }
        
        String key = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Error checking idempotency key existence in Redis: {}", idempotencyKey, e);
            return false;
        }
    }
}

