package com.aaami.user.handler;

import com.aaami.shared.dto.PaginatedResponse;
import com.aaami.shared.dto.UserDto;
import com.aaami.shared.dto.UserRole;
import com.aaami.user.domain.User;
import com.aaami.user.mapper.UserMapper;
import com.aaami.user.query.GetAllUsersQuery;
import com.aaami.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetAllUsersQueryHandler Tests")
class GetAllUsersQueryHandlerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private GetAllUsersQueryHandler handler;

    private GetAllUsersQuery query;
    private User user1;
    private User user2;
    private UserDto userDto1;
    private UserDto userDto2;

    @BeforeEach
    void setUp() {
        query = new GetAllUsersQuery();

        user1 = User.builder()
                .id(1L)
                .email("user1@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.USER)
                .build();

        user2 = User.builder()
                .id(2L)
                .email("user2@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .role(UserRole.PREMIUM_USER)
                .build();

        userDto1 = UserDto.builder()
                .id(1L)
                .email("user1@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.USER)
                .build();

        userDto2 = UserDto.builder()
                .id(2L)
                .email("user2@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .role(UserRole.PREMIUM_USER)
                .build();
    }

    @Test
    @DisplayName("Should return paginated users with default pagination")
    void handle_ShouldReturnPaginatedUsers_WithDefaultPagination() {
        // Given
        Page<User> userPage = new PageImpl<>(List.of(user1, user2), PageRequest.of(0, 20), 2);
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toDto(user1)).thenReturn(userDto1);
        when(userMapper.toDto(user2)).thenReturn(userDto2);

        // When
        PaginatedResponse<UserDto> result = handler.handle(query);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(0, result.getPage());
        assertEquals(20, result.getSize());
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertTrue(result.isFirst());
        assertTrue(result.isLast());
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Should return paginated users with custom pagination")
    void handle_ShouldReturnPaginatedUsers_WithCustomPagination() {
        // Given
        query.setPage(1);
        query.setSize(10);
        Page<User> userPage = new PageImpl<>(List.of(user2), PageRequest.of(1, 10), 2);
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toDto(user2)).thenReturn(userDto2);

        // When
        PaginatedResponse<UserDto> result = handler.handle(query);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getPage());
        assertEquals(10, result.getSize());
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Should filter by firstName when provided")
    void handle_ShouldFilterByFirstName_WhenProvided() {
        // Given
        query.setFirstName("John");
        Page<User> userPage = new PageImpl<>(List.of(user1), PageRequest.of(0, 20), 1);
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toDto(user1)).thenReturn(userDto1);

        // When
        PaginatedResponse<UserDto> result = handler.handle(query);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("John", result.getContent().get(0).getFirstName());
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Should filter by role when provided")
    void handle_ShouldFilterByRole_WhenProvided() {
        // Given
        query.setRole(UserRole.USER);
        Page<User> userPage = new PageImpl<>(List.of(user1), PageRequest.of(0, 20), 1);
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toDto(user1)).thenReturn(userDto1);

        // When
        PaginatedResponse<UserDto> result = handler.handle(query);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(UserRole.USER, result.getContent().get(0).getRole());
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Should sort by provided field and direction")
    void handle_ShouldSortByFieldAndDirection_WhenProvided() {
        // Given
        query.setSortBy("email");
        query.setSortDirection("desc");
        Page<User> userPage = new PageImpl<>(List.of(user2, user1), PageRequest.of(0, 20), 2);
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toDto(user2)).thenReturn(userDto2);
        when(userMapper.toDto(user1)).thenReturn(userDto1);

        // When
        PaginatedResponse<UserDto> result = handler.handle(query);

        // Then
        assertNotNull(result);
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Should use default sort when sortBy is not provided")
    void handle_ShouldUseDefaultSort_WhenSortByNotProvided() {
        // Given
        Page<User> userPage = new PageImpl<>(List.of(user1, user2), PageRequest.of(0, 20), 2);
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toDto(user1)).thenReturn(userDto1);
        when(userMapper.toDto(user2)).thenReturn(userDto2);

        // When
        PaginatedResponse<UserDto> result = handler.handle(query);

        // Then
        assertNotNull(result);
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }
}

