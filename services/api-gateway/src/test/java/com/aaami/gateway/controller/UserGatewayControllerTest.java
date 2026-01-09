package com.aaami.gateway.controller;

import com.aaami.gateway.client.UserServiceClient;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserGatewayController Tests")
class UserGatewayControllerTest {

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private UserGatewayController controller;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
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
    void createUser_ShouldReturnCreated() {
        // Given
        CreateUserCommand command = new CreateUserCommand();
        when(userServiceClient.createUser(any(CreateUserCommand.class))).thenReturn(userDto);

        // When
        ResponseEntity<UserDto> response = controller.createUser(command);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(userServiceClient).createUser(any(CreateUserCommand.class));
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
        when(userServiceClient.getAllUsers(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(paginatedResponse);

        // When
        ResponseEntity<PaginatedResponse<UserDto>> response = controller.getAllUsers(
                null, null, null, null, 0, 20, null, "asc");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        verify(userServiceClient).getAllUsers(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should get user by ID")
    void getUser_ShouldReturnUser() {
        // Given
        when(userServiceClient.getUser(1L)).thenReturn(userDto);

        // When
        ResponseEntity<UserDto> response = controller.getUser(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(userServiceClient).getUser(1L);
    }

    @Test
    @DisplayName("Should get user by email")
    void getUserByEmail_ShouldReturnUser() {
        // Given
        when(userServiceClient.getUserByEmail("test@example.com")).thenReturn(userDto);

        // When
        ResponseEntity<UserDto> response = controller.getUserByEmail("test@example.com");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test@example.com", response.getBody().getEmail());
        verify(userServiceClient).getUserByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should update user successfully")
    void updateUser_ShouldReturnUpdatedUser() {
        // Given
        UpdateUserCommand command = new UpdateUserCommand();
        when(userServiceClient.updateUser(anyLong(), any(UpdateUserCommand.class))).thenReturn(userDto);

        // When
        ResponseEntity<UserDto> response = controller.updateUser(1L, command);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userServiceClient).updateUser(1L, command);
    }

    @Test
    @DisplayName("Should delete user successfully")
    void deleteUser_ShouldReturnNoContent() {
        // Given
        doNothing().when(userServiceClient).deleteUser(1L);

        // When
        ResponseEntity<Void> response = controller.deleteUser(1L);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userServiceClient).deleteUser(1L);
    }
}

