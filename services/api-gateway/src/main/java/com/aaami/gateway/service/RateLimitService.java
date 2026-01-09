package com.aaami.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service for rate limiting using Redis sliding window counter.
 * 
 * Implements sliding window rate limiting:
 * - Each key (e.g., IP address or email) has a counter
 * - Counter increments on each request
 * - Counter expires after the time window
 * - If counter exceeds limit, request is blocked
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {
    
    private static final String RATE_LIMIT_KEY_PREFIX = "ratelimit:";
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Value("${rate-limit.login.max-requests:5}")
    private int maxRequests;
    
    @Value("${rate-limit.login.window-minutes:15}")
    private int windowMinutes;
    
    public int getMaxRequests() {
        return maxRequests;
    }
    
    public int getWindowMinutes() {
        return windowMinutes;
    }
    
    /**
     * Checks if a request is allowed based on rate limiting.
     * 
     * @param key The rate limit key (e.g., IP address, email, user ID)
     * @return true if request is allowed, false if rate limit exceeded
     */
    public boolean isAllowed(String key) {
        return isAllowed(key, maxRequests, windowMinutes);
    }
    
    /**
     * Checks if a request is allowed with custom limits.
     * 
     * @param key The rate limit key
     * @param maxRequests Maximum number of requests allowed
     * @param windowMinutes Time window in minutes
     * @return true if request is allowed, false if rate limit exceeded
     */
    public boolean isAllowed(String key, int maxRequests, int windowMinutes) {
        String rateLimitKey = buildRateLimitKey(key);
        
        try {
            ValueOperations<String, Object> ops = redisTemplate.opsForValue();
            
            // Get current count
            Object currentCountObj = ops.get(rateLimitKey);
            int currentCount = currentCountObj != null ? ((Number) currentCountObj).intValue() : 0;
            
            // Check if limit exceeded
            if (currentCount >= maxRequests) {
                log.warn("Rate limit exceeded for key: {} ({} requests in {} minutes)", 
                    key, currentCount, windowMinutes);
                return false;
            }
            
            // Increment counter
            // If key doesn't exist, set it with expiration
            // If key exists, increment it (expiration is preserved)
            if (currentCount == 0) {
                // First request in the window - set with expiration
                ops.set(rateLimitKey, 1, windowMinutes, TimeUnit.MINUTES);
            } else {
                // Increment existing counter
                ops.increment(rateLimitKey);
            }
            
            log.debug("Rate limit check passed for key: {} (count: {}/{})", 
                key, currentCount + 1, maxRequests);
            return true;
            
        } catch (Exception e) {
            log.error("Error checking rate limit for key: {}", key, e);
            // On error, allow the request (fail open) to avoid blocking legitimate users
            // In production, you might want to fail closed depending on your requirements
            return true;
        }
    }
    
    /**
     * Gets the remaining requests for a key.
     * 
     * @param key The rate limit key
     * @return Number of remaining requests, or maxRequests if key doesn't exist
     */
    public int getRemainingRequests(String key) {
        String rateLimitKey = buildRateLimitKey(key);
        
        try {
            Object currentCountObj = redisTemplate.opsForValue().get(rateLimitKey);
            int currentCount = currentCountObj != null ? ((Number) currentCountObj).intValue() : 0;
            return Math.max(0, maxRequests - currentCount);
        } catch (Exception e) {
            log.error("Error getting remaining requests for key: {}", key, e);
            return maxRequests; // Return max on error
        }
    }
    
    /**
     * Resets the rate limit counter for a key (useful for testing or admin operations).
     * 
     * @param key The rate limit key to reset
     */
    public void resetRateLimit(String key) {
        String rateLimitKey = buildRateLimitKey(key);
        try {
            redisTemplate.delete(rateLimitKey);
            log.debug("Rate limit reset for key: {}", key);
        } catch (Exception e) {
            log.error("Error resetting rate limit for key: {}", key, e);
        }
    }
    
    private String buildRateLimitKey(String key) {
        return RATE_LIMIT_KEY_PREFIX + key;
    }
}

