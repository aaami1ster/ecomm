package com.aaami.user.handler;

import com.aaami.shared.dto.UserDto;
import com.aaami.shared.dto.UserRole;
import com.aaami.user.domain.User;
import com.aaami.user.exception.UserNotFoundException;
import com.aaami.user.mapper.UserMapper;
import com.aaami.user.query.GetUserByEmailQuery;
import com.aaami.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetUserByEmailQueryHandler Tests")
class GetUserByEmailQueryHandlerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private GetUserByEmailQueryHandler handler;

    private GetUserByEmailQuery query;
    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        query = new GetUserByEmailQuery();
        query.setEmail("test@example.com");

        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.USER)
                .build();

        userDto = UserDto.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.USER)
                .build();
    }

    @Test
    @DisplayName("Should return user DTO when user exists")
    void handle_ShouldReturnUserDto_WhenUserExists() {
        // Given
        when(userRepository.findByEmailAndDeletedAtIsNull("test@example.com")).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto);

        // When
        UserDto result = handler.handle(query);

        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).findByEmailAndDeletedAtIsNull("test@example.com");
        verify(userMapper).toDto(user);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user does not exist")
    void handle_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findByEmailAndDeletedAtIsNull("test@example.com")).thenReturn(Optional.empty());

        // When & Then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> handler.handle(query));
        assertEquals("User not found with email: test@example.com", exception.getMessage());
        verify(userRepository).findByEmailAndDeletedAtIsNull("test@example.com");
        verify(userMapper, never()).toDto(any());
    }
}

