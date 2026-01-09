package com.aaami.user.handler;

import com.aaami.shared.command.CreateUserCommand;
import com.aaami.shared.dto.UserDto;
import com.aaami.shared.dto.UserRole;
import com.aaami.user.domain.User;
import com.aaami.user.exception.DuplicateEmailException;
import com.aaami.user.mapper.UserMapper;
import com.aaami.user.repository.UserRepository;
import com.aaami.user.service.PasswordEncoder;
import com.aaami.user.service.UserEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateUserCommandHandlerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserEventProducer eventProducer;

    @InjectMocks
    private CreateUserCommandHandler handler;

    private CreateUserCommand command;
    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        command = new CreateUserCommand();
        command.setEmail("test@example.com");
        command.setPassword("password123");
        command.setFirstName("John");
        command.setLastName("Doe");
        command.setRole(UserRole.USER);

        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy") // BCrypt hash for password123
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
    void handle_ShouldCreateUser_WhenEmailDoesNotExist() {
        // Given
        when(userRepository.existsByEmailAndDeletedAtIsNull(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"); // BCrypt hash
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);
        doNothing().when(eventProducer).publishUserCreated(any(UserDto.class));

        // When
        UserDto result = handler.handle(command);

        // Then
        assertNotNull(result);
        assertEquals(userDto.getId(), result.getId());
        assertEquals(userDto.getEmail(), result.getEmail());
        verify(userRepository).existsByEmailAndDeletedAtIsNull(command.getEmail());
        verify(passwordEncoder).encode(command.getPassword());
        verify(userRepository).save(any(User.class));
        verify(userMapper).toDto(any(User.class));
    }

    @Test
    void handle_ShouldThrowDuplicateEmailException_WhenEmailExists() {
        // Given
        when(userRepository.existsByEmailAndDeletedAtIsNull(anyString())).thenReturn(true);

        // When & Then
        assertThrows(DuplicateEmailException.class, () -> handler.handle(command));
        verify(userRepository).existsByEmailAndDeletedAtIsNull(command.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void handle_ShouldUseDefaultRole_WhenRoleIsNull() {
        // Given
        command.setRole(null);
        when(userRepository.existsByEmailAndDeletedAtIsNull(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"); // BCrypt hash
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);
        doNothing().when(eventProducer).publishUserCreated(any(UserDto.class));

        // When
        handler.handle(command);

        // Then
        verify(userRepository).save(argThat(u -> u.getRole() == UserRole.USER));
    }

    @Test
    void handle_ShouldUseProvidedRole_WhenRoleIsProvided() {
        // Given
        command.setRole(UserRole.ADMIN);
        when(userRepository.existsByEmailAndDeletedAtIsNull(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"); // BCrypt hash
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);
        doNothing().when(eventProducer).publishUserCreated(any(UserDto.class));

        // When
        handler.handle(command);

        // Then
        verify(userRepository).save(argThat(u -> u.getRole() == UserRole.ADMIN));
    }
}

