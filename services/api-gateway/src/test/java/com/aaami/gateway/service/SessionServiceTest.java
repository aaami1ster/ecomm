package com.aaami.gateway.service;

import com.aaami.shared.dto.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SessionService Tests")
class SessionServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private SessionService sessionService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Should create session successfully")
    void createSession_ShouldStoreSessionInRedis() {
        // Given
        String token = "test-token";
        Long userId = 1L;
        String email = "test@example.com";
        UserRole role = UserRole.USER;
        long expiration = 86400000L;

        // When
        sessionService.createSession(token, userId, email, role, expiration);

        // Then
        verify(redisTemplate.opsForValue()).set(
                eq("token:test-token"),
                any(SessionService.SessionInfo.class),
                eq(expiration),
                eq(TimeUnit.MILLISECONDS)
        );
        verify(redisTemplate.opsForValue()).set(
                eq("session:1:test-token"),
                eq(token),
                eq(expiration),
                eq(TimeUnit.MILLISECONDS)
        );
    }

    @Test
    @DisplayName("Should return true when session is valid")
    void isValidSession_ShouldReturnTrue_WhenSessionExists() {
        // Given
        String token = "test-token";
        when(valueOperations.get("token:test-token")).thenReturn(new SessionService.SessionInfo(1L, "test@example.com", UserRole.USER));

        // When
        boolean isValid = sessionService.isValidSession(token);

        // Then
        assertTrue(isValid);
        verify(valueOperations).get("token:test-token");
    }

    @Test
    @DisplayName("Should return false when session does not exist")
    void isValidSession_ShouldReturnFalse_WhenSessionNotFound() {
        // Given
        String token = "invalid-token";
        when(valueOperations.get("token:invalid-token")).thenReturn(null);

        // When
        boolean isValid = sessionService.isValidSession(token);

        // Then
        assertFalse(isValid);
        verify(valueOperations).get("token:invalid-token");
    }

    @Test
    @DisplayName("Should invalidate session")
    void invalidateSession_ShouldDeleteSessionFromRedis() {
        // Given
        String token = "test-token";
        SessionService.SessionInfo sessionInfo = new SessionService.SessionInfo(1L, "test@example.com", UserRole.USER);
        when(valueOperations.get("token:test-token")).thenReturn(sessionInfo);

        // When
        sessionService.invalidateSession(token);

        // Then
        verify(valueOperations).get("token:test-token");
        verify(redisTemplate).delete("token:test-token");
        verify(redisTemplate).delete("session:1:test-token");
    }

    @Test
    @DisplayName("Should get session information")
    void getSession_ShouldReturnSessionInfo_WhenSessionExists() {
        // Given
        String token = "test-token";
        SessionService.SessionInfo sessionInfo = new SessionService.SessionInfo(1L, "test@example.com", UserRole.USER);
        when(valueOperations.get("token:test-token")).thenReturn(sessionInfo);

        // When
        SessionService.SessionInfo result = sessionService.getSession(token);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("test@example.com", result.getEmail());
        verify(valueOperations).get("token:test-token");
    }

    @Test
    @DisplayName("Should return null when getting non-existent session")
    void getSession_ShouldReturnNull_WhenSessionNotFound() {
        // Given
        String token = "invalid-token";
        when(valueOperations.get("token:invalid-token")).thenReturn(null);

        // When
        SessionService.SessionInfo result = sessionService.getSession(token);

        // Then
        assertNull(result);
        verify(valueOperations).get("token:invalid-token");
    }
}

