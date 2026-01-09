package com.aaami.gateway.service;

import com.aaami.shared.dto.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {
    
    private static final String SESSION_PREFIX = "session:";
    private static final String TOKEN_PREFIX = "token:";
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * Stores a session in Redis with the token as key
     * @param token JWT token
     * @param userId User ID
     * @param email User email
     * @param role User role
     * @param expirationInMillis Session expiration time in milliseconds
     */
    public void createSession(String token, Long userId, String email, UserRole role, long expirationInMillis) {
        String tokenKey = TOKEN_PREFIX + token;
        SessionInfo sessionInfo = new SessionInfo(userId, email, role);
        
        try {
            redisTemplate.opsForValue().set(tokenKey, sessionInfo, expirationInMillis, TimeUnit.MILLISECONDS);
            
            // Also store user sessions mapping for quick lookup
            String userSessionKey = SESSION_PREFIX + userId + ":" + token;
            redisTemplate.opsForValue().set(userSessionKey, token, expirationInMillis, TimeUnit.MILLISECONDS);
            
            log.debug("Session created for user {} with token key {}", userId, tokenKey);
        } catch (Exception e) {
            log.error("Error creating session in Redis for user {}", userId, e);
            throw new RuntimeException("Failed to create session", e);
        }
    }
    
    /**
     * Validates if a session exists in Redis
     * @param token JWT token
     * @return true if session exists, false otherwise
     */
    public boolean isValidSession(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        try {
            Object session = redisTemplate.opsForValue().get(tokenKey);
            return session != null;
        } catch (Exception e) {
            log.error("Error checking session validity for token", e);
            return false;
        }
    }
    
    /**
     * Gets session information from Redis
     * @param token JWT token
     * @return SessionInfo if session exists, null otherwise
     */
    public SessionInfo getSession(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        try {
            return (SessionInfo) redisTemplate.opsForValue().get(tokenKey);
        } catch (Exception e) {
            log.error("Error retrieving session for token", e);
            return null;
        }
    }
    
    /**
     * Invalidates a session by removing it from Redis
     * @param token JWT token
     */
    public void invalidateSession(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        try {
            SessionInfo sessionInfo = getSession(token);
            if (sessionInfo != null) {
                // Delete token mapping
                redisTemplate.delete(tokenKey);
                
                // Delete user session mapping
                String userSessionKey = SESSION_PREFIX + sessionInfo.getUserId() + ":" + token;
                redisTemplate.delete(userSessionKey);
                
                log.debug("Session invalidated for token {}", tokenKey);
            }
        } catch (Exception e) {
            log.error("Error invalidating session for token", e);
        }
    }
    
    /**
     * Invalidates all sessions for a user
     * @param userId User ID
     */
    public void invalidateAllUserSessions(Long userId) {
        try {
            // Find all session keys for this user
            String pattern = SESSION_PREFIX + userId + ":*";
            var keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                // Get tokens from user session keys and delete token mappings
                for (String userSessionKey : keys) {
                    String token = (String) redisTemplate.opsForValue().get(userSessionKey);
                    if (token != null) {
                        String tokenKey = TOKEN_PREFIX + token;
                        redisTemplate.delete(tokenKey);
                    }
                    redisTemplate.delete(userSessionKey);
                }
                log.debug("All sessions invalidated for user {}", userId);
            }
        } catch (Exception e) {
            log.error("Error invalidating all sessions for user {}", userId, e);
        }
    }
    
    /**
     * Session information stored in Redis
     */
    public static class SessionInfo {
        private Long userId;
        private String email;
        private UserRole role;
        
        public SessionInfo() {
        }
        
        public SessionInfo(Long userId, String email, UserRole role) {
            this.userId = userId;
            this.email = email;
            this.role = role;
        }
        
        public Long getUserId() {
            return userId;
        }
        
        public void setUserId(Long userId) {
            this.userId = userId;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public UserRole getRole() {
            return role;
        }
        
        public void setRole(UserRole role) {
            this.role = role;
        }
    }
}

