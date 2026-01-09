package com.aaami.user.handler;

import com.aaami.shared.command.UpdateUserCommand;
import com.aaami.shared.dto.UserDto;
import com.aaami.shared.dto.UserRole;
import com.aaami.user.domain.User;
import com.aaami.user.exception.DuplicateEmailException;
import com.aaami.user.exception.UserNotFoundException;
import com.aaami.user.mapper.UserMapper;
import com.aaami.user.repository.UserRepository;
import com.aaami.user.service.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateUserCommandHandlerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UpdateUserCommandHandler handler;

    private UpdateUserCommand command;
    private User existingUser;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        command = new UpdateUserCommand();
        command.setId(1L);

        existingUser = User.builder()
                .id(1L)
                .email("old@example.com")
                .password("old_encoded_password")
                .firstName("Old")
                .lastName("Name")
                .role(UserRole.USER)
                .build();

        userDto = UserDto.builder()
                .id(1L)
                .email("new@example.com")
                .firstName("New")
                .lastName("Name")
                .role(UserRole.ADMIN)
                .build();
    }

    @Test
    void handle_ShouldUpdateUser_WhenUserExists() {
        // Given
        command.setFirstName("New");
        command.setLastName("Name");
        when(userRepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        // When
        UserDto result = handler.handle(command);

        // Then
        assertNotNull(result);
        verify(userRepository).findByIdAndDeletedAtIsNull(command.getId());
        verify(userRepository).save(existingUser);
        assertEquals("New", existingUser.getFirstName());
        assertEquals("Name", existingUser.getLastName());
    }

    @Test
    void handle_ShouldThrowUserNotFoundException_WhenUserDoesNotExist() {
        // Given
        when(userRepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> handler.handle(command));
        verify(userRepository).findByIdAndDeletedAtIsNull(command.getId());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void handle_ShouldUpdateEmail_WhenEmailIsProvidedAndDifferent() {
        // Given
        command.setEmail("new@example.com");
        when(userRepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmailAndDeletedAtIsNull(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(existingUser);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        // When
        handler.handle(command);

        // Then
        verify(userRepository).existsByEmailAndDeletedAtIsNull("new@example.com");
        verify(userRepository).save(existingUser);
        assertEquals("new@example.com", existingUser.getEmail());
    }

    @Test
    void handle_ShouldThrowDuplicateEmailException_WhenNewEmailExists() {
        // Given
        command.setEmail("existing@example.com");
        when(userRepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmailAndDeletedAtIsNull(anyString())).thenReturn(true);

        // When & Then
        assertThrows(DuplicateEmailException.class, () -> handler.handle(command));
        verify(userRepository).existsByEmailAndDeletedAtIsNull("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void handle_ShouldUpdatePassword_WhenPasswordIsProvided() {
        // Given
        command.setPassword("newPassword");
        String bcryptHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"; // BCrypt hash for newPassword
        when(userRepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode(anyString())).thenReturn(bcryptHash);
        when(userRepository.save(any(User.class))).thenReturn(existingUser);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        // When
        handler.handle(command);

        // Then
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(existingUser);
        assertEquals(bcryptHash, existingUser.getPassword());
    }

    @Test
    void handle_ShouldUpdateRole_WhenRoleIsProvided() {
        // Given
        command.setRole(UserRole.ADMIN);
        when(userRepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        // When
        handler.handle(command);

        // Then
        verify(userRepository).save(existingUser);
        assertEquals(UserRole.ADMIN, existingUser.getRole());
    }

    @Test
    void handle_ShouldNotUpdateEmail_WhenEmailIsSame() {
        // Given
        command.setEmail("old@example.com");
        when(userRepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        // When
        handler.handle(command);

        // Then
        verify(userRepository, never()).existsByEmailAndDeletedAtIsNull(anyString());
    }
}

