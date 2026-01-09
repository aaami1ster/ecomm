package com.aaami.order.client;

import com.aaami.config.ServiceProperties;
import com.aaami.shared.dto.UserDto;
import com.aaami.shared.dto.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceClient Tests")
class UserServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ServiceProperties serviceProperties;

    @InjectMocks
    private UserServiceClient client;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        when(serviceProperties.getUserServiceUrl()).thenReturn("http://localhost:8083");

        userDto = UserDto.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.USER)
                .build();
    }

    @Test
    @DisplayName("Should get user by ID")
    void getUser_ShouldReturnUser_WhenUserExists() {
        // Given
        ResponseEntity<UserDto> response = new ResponseEntity<>(userDto, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(UserDto.class))).thenReturn(response);

        // When
        UserDto result = client.getUser(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getEmail());
        verify(restTemplate).getForEntity("http://localhost:8083/api/users/1", UserDto.class);
    }

    @Test
    @DisplayName("Should return user DTO from response body")
    void getUser_ShouldReturnUserFromResponseBody() {
        // Given
        ResponseEntity<UserDto> response = new ResponseEntity<>(userDto, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(UserDto.class))).thenReturn(response);

        // When
        UserDto result = client.getUser(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(restTemplate).getForEntity("http://localhost:8083/api/users/1", UserDto.class);
    }
}

