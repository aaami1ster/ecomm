package com.aaami.user.controller;

import com.aaami.cqrs.CommandBus;
import com.aaami.cqrs.QueryBus;
import com.aaami.shared.command.CreateUserCommand;
import com.aaami.shared.command.DeleteUserCommand;
import com.aaami.shared.command.UpdateUserCommand;
import com.aaami.shared.dto.PaginatedResponse;
import com.aaami.shared.dto.UserDto;
import com.aaami.shared.dto.UserRole;
import com.aaami.user.query.GetAllUsersQuery;
import com.aaami.user.query.GetUserByEmailQuery;
import com.aaami.user.query.GetUserQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private CommandBus commandBus;

    @Mock
    private QueryBus queryBus;

    @InjectMocks
    private UserController controller;

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
    void createUser_ShouldReturnCreatedStatus_WhenUserIsCreated() {
        // Given
        CreateUserCommand command = new CreateUserCommand();
        command.setEmail("test@example.com");
        command.setPassword("password");
        command.setFirstName("John");
        command.setLastName("Doe");

        when(commandBus.dispatch(any(CreateUserCommand.class))).thenReturn(userDto);

        // When
        ResponseEntity<UserDto> response = controller.createUser(command);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userDto.getId(), response.getBody().getId());
        verify(commandBus).dispatch(command);
    }

    @Test
    void getUser_ShouldReturnUser_WhenUserExists() {
        // Given
        Long userId = 1L;
        when(queryBus.dispatch(any(GetUserQuery.class))).thenReturn(userDto);

        // When
        ResponseEntity<UserDto> response = controller.getUser(userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userDto.getId(), response.getBody().getId());
        verify(queryBus).dispatch(any(GetUserQuery.class));
    }

    @Test
    void getUserByEmail_ShouldReturnUser_WhenUserExists() {
        // Given
        String email = "test@example.com";
        when(queryBus.dispatch(any(GetUserByEmailQuery.class))).thenReturn(userDto);

        // When
        ResponseEntity<UserDto> response = controller.getUserByEmail(email);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(email, response.getBody().getEmail());
        verify(queryBus).dispatch(any(GetUserByEmailQuery.class));
    }

    @Test
    void getAllUsers_ShouldReturnPaginatedResponse() {
        // Given
        PaginatedResponse<UserDto> paginatedResponse = PaginatedResponse.<UserDto>builder()
                .content(List.of(userDto))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();

        when(queryBus.dispatch(any(GetAllUsersQuery.class))).thenReturn(paginatedResponse);

        // When
        ResponseEntity<PaginatedResponse<UserDto>> response = controller.getAllUsers(
                null, null, null, null, 0, 20, null, "asc");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        verify(queryBus).dispatch(any(GetAllUsersQuery.class));
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser_WhenUpdateSucceeds() {
        // Given
        Long userId = 1L;
        UpdateUserCommand command = new UpdateUserCommand();
        command.setId(userId);
        command.setFirstName("Updated");

        when(commandBus.dispatch(any(UpdateUserCommand.class))).thenReturn(userDto);

        // When
        ResponseEntity<UserDto> response = controller.updateUser(userId, command);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userId, command.getId());
        verify(commandBus).dispatch(command);
    }

    @Test
    void deleteUser_ShouldReturnNoContent_WhenDeleteSucceeds() {
        // Given
        Long userId = 1L;
        doNothing().when(commandBus).dispatch(any(DeleteUserCommand.class));

        // When
        ResponseEntity<Void> response = controller.deleteUser(userId);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(commandBus).dispatch(any(DeleteUserCommand.class));
    }
}

