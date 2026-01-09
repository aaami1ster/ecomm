package com.aaami.user.mapper;

import com.aaami.shared.dto.UserDto;
import com.aaami.shared.dto.UserRole;
import com.aaami.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private UserMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UserMapper();
    }

    @Test
    void toDto_ShouldMapUserToDto_WhenUserIsNotNull() {
        // Given
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.USER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // When
        UserDto dto = mapper.toDto(user);

        // Then
        assertNotNull(dto);
        assertEquals(user.getId(), dto.getId());
        assertEquals(user.getEmail(), dto.getEmail());
        assertEquals(user.getFirstName(), dto.getFirstName());
        assertEquals(user.getLastName(), dto.getLastName());
        assertEquals(user.getRole(), dto.getRole());
        assertEquals(user.getCreatedAt(), dto.getCreatedAt());
        assertEquals(user.getUpdatedAt(), dto.getUpdatedAt());
    }

    @Test
    void toDto_ShouldReturnNull_WhenUserIsNull() {
        // When
        UserDto dto = mapper.toDto(null);

        // Then
        assertNull(dto);
    }

    @Test
    void toEntity_ShouldMapDtoToUser_WhenDtoIsNotNull() {
        // Given
        UserDto dto = UserDto.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.ADMIN)
                .build();

        // When
        User user = mapper.toEntity(dto);

        // Then
        assertNotNull(user);
        assertEquals(dto.getId(), user.getId());
        assertEquals(dto.getEmail(), user.getEmail());
        assertEquals(dto.getFirstName(), user.getFirstName());
        assertEquals(dto.getLastName(), user.getLastName());
        assertEquals(dto.getRole(), user.getRole());
    }

    @Test
    void toEntity_ShouldReturnNull_WhenDtoIsNull() {
        // When
        User user = mapper.toEntity(null);

        // Then
        assertNull(user);
    }

    @Test
    void toDto_ShouldHandleAllRoles() {
        // Given
        for (UserRole role : UserRole.values()) {
            User user = User.builder()
                    .id(1L)
                    .email("test@example.com")
                    .firstName("John")
                    .lastName("Doe")
                    .role(role)
                    .build();

            // When
            UserDto dto = mapper.toDto(user);

            // Then
            assertEquals(role, dto.getRole());
        }
    }
}

