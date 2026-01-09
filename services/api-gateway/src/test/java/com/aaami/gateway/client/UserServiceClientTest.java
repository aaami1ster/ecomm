package com.aaami.gateway.client;

import com.aaami.gateway.config.ServiceProperties;
import com.aaami.shared.command.CreateUserCommand;
import com.aaami.shared.command.UpdateUserCommand;
import com.aaami.shared.dto.PaginatedResponse;
import com.aaami.shared.dto.UserDto;
import com.aaami.shared.dto.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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
    @DisplayName("Should create user successfully")
    void createUser_ShouldReturnUserDto() {
        // Given
        CreateUserCommand command = new CreateUserCommand();
        ResponseEntity<UserDto> response = new ResponseEntity<>(userDto, HttpStatus.CREATED);
        when(restTemplate.postForEntity(anyString(), any(CreateUserCommand.class), eq(UserDto.class)))
                .thenReturn(response);

        // When
        UserDto result = client.createUser(command);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(restTemplate).postForEntity("http://localhost:8083/api/users", command, UserDto.class);
    }

    @Test
    @DisplayName("Should get user by ID")
    void getUser_ShouldReturnUserDto() {
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

    @Test
    @DisplayName("Should get user by email")
    void getUserByEmail_ShouldReturnUserDto() {
        // Given
        ResponseEntity<UserDto> response = new ResponseEntity<>(userDto, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(UserDto.class))).thenReturn(response);

        // When
        UserDto result = client.getUserByEmail("test@example.com");

        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(restTemplate).getForEntity("http://localhost:8083/api/users/email/test@example.com", UserDto.class);
    }

    @Test
    @DisplayName("Should get all users with pagination")
    void getAllUsers_ShouldReturnPaginatedResponse() {
        // Given
        PaginatedResponse<UserDto> paginatedResponse = PaginatedResponse.<UserDto>builder()
                .content(java.util.List.of(userDto))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();
        ResponseEntity<PaginatedResponse<UserDto>> response = new ResponseEntity<>(paginatedResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        // When
        PaginatedResponse<UserDto> result = client.getAllUsers(null, null, null, null, null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class));
    }

    @Test
    @DisplayName("Should update user successfully")
    void updateUser_ShouldReturnUpdatedUserDto() {
        // Given
        UpdateUserCommand command = new UpdateUserCommand();
        ResponseEntity<UserDto> response = new ResponseEntity<>(userDto, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(UserDto.class)))
                .thenReturn(response);

        // When
        UserDto result = client.updateUser(1L, command);

        // Then
        assertNotNull(result);
        verify(restTemplate).exchange(
                eq("http://localhost:8083/api/users/1"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(UserDto.class)
        );
    }

    @Test
    @DisplayName("Should delete user successfully")
    void deleteUser_ShouldCallDeleteEndpoint() {
        // When
        client.deleteUser(1L);

        // Then
        verify(restTemplate).delete("http://localhost:8083/api/users/1");
    }
}

