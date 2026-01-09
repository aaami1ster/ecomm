package com.aaami.user.handler;

import com.aaami.shared.command.DeleteUserCommand;
import com.aaami.user.domain.User;
import com.aaami.user.exception.UserNotFoundException;
import com.aaami.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteUserCommandHandler Tests")
class DeleteUserCommandHandlerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DeleteUserCommandHandler handler;

    private DeleteUserCommand command;
    private User user;

    @BeforeEach
    void setUp() {
        command = new DeleteUserCommand();
        command.setId(1L);

        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();
    }

    @Test
    @DisplayName("Should soft delete user when user exists")
    void handle_ShouldSoftDeleteUser_WhenUserExists() {
        // Given
        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        handler.handle(command);

        // Then
        assertNotNull(user.getDeletedAt());
        verify(userRepository).findByIdAndDeletedAtIsNull(1L);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user does not exist")
    void handle_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        // When & Then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> handler.handle(command));
        assertEquals("User not found with id: 1", exception.getMessage());
        verify(userRepository).findByIdAndDeletedAtIsNull(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should set deletedAt timestamp when deleting user")
    void handle_ShouldSetDeletedAtTimestamp_WhenDeletingUser() {
        // Given
        LocalDateTime beforeDelete = LocalDateTime.now();
        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            return savedUser;
        });

        // When
        handler.handle(command);

        // Then
        assertNotNull(user.getDeletedAt());
        assertTrue(user.getDeletedAt().isAfter(beforeDelete.minusSeconds(1)));
        assertTrue(user.getDeletedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
}

