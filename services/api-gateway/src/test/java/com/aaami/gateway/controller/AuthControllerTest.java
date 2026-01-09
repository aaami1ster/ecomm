package com.aaami.gateway.controller;

import com.aaami.gateway.client.UserServiceClient;
import com.aaami.gateway.security.JwtTokenProvider;
import com.aaami.shared.dto.UserDto;
import com.aaami.shared.dto.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController controller;

    private AuthController.LoginRequest loginRequest;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        loginRequest = new AuthController.LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        userDto = UserDto.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.USER)
                .build();
    }

    @Test
    void login_ShouldReturnToken_WhenUserExists() {
        // Given
        String expectedToken = "test-jwt-token";
        when(userServiceClient.getUserByEmail(anyString())).thenReturn(userDto);
        when(tokenProvider.generateToken(any(), anyString(), any())).thenReturn(expectedToken);

        // When
        ResponseEntity<Map<String, Object>> response = controller.login(loginRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedToken, response.getBody().get("token"));
        assertEquals("Bearer", response.getBody().get("type"));
        verify(userServiceClient).getUserByEmail(loginRequest.getEmail());
        verify(tokenProvider).generateToken(userDto.getId(), userDto.getEmail(), userDto.getRole());
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenUserNotFound() {
        // Given - Return null to simulate user not found
        when(userServiceClient.getUserByEmail(anyString())).thenReturn(null);

        // When
        ResponseEntity<Map<String, Object>> response = controller.login(loginRequest);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenHttpClientErrorExceptionNotFound() {
        // Given - Throw HttpClientErrorException with 404 status
        when(userServiceClient.getUserByEmail(anyString()))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not found"));

        // When
        ResponseEntity<Map<String, Object>> response = controller.login(loginRequest);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenExceptionOccurs() {
        // Given
        when(userServiceClient.getUserByEmail(anyString())).thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<Map<String, Object>> response = controller.login(loginRequest);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
    }
}

