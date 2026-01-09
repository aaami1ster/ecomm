package com.aaami.gateway.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SecurityUser Tests")
class SecurityUserTest {

    private SecurityUser securityUser;

    @BeforeEach
    void setUp() {
        securityUser = new SecurityUser(1L, "test@example.com", "ROLE_USER", 
                java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    @DisplayName("Should return correct username")
    void getUsername_ShouldReturnEmail() {
        // When
        String username = securityUser.getUsername();

        // Then
        assertEquals("test@example.com", username);
    }

    @Test
    @DisplayName("Should return null password")
    void getPassword_ShouldReturnNull() {
        // When
        String password = securityUser.getPassword();

        // Then
        assertNull(password);
    }

    @Test
    @DisplayName("Should return correct authorities")
    void getAuthorities_ShouldReturnRole() {
        // When
        Collection<? extends GrantedAuthority> authorities = securityUser.getAuthorities();

        // Then
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    @DisplayName("Should return true for account non-expired")
    void isAccountNonExpired_ShouldReturnTrue() {
        // When
        boolean result = securityUser.isAccountNonExpired();

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return true for account non-locked")
    void isAccountNonLocked_ShouldReturnTrue() {
        // When
        boolean result = securityUser.isAccountNonLocked();

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return true for credentials non-expired")
    void isCredentialsNonExpired_ShouldReturnTrue() {
        // When
        boolean result = securityUser.isCredentialsNonExpired();

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return true for enabled")
    void isEnabled_ShouldReturnTrue() {
        // When
        boolean result = securityUser.isEnabled();

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return correct user ID")
    void getUserId_ShouldReturnId() {
        // When
        Long userId = securityUser.getUserId();

        // Then
        assertEquals(1L, userId);
    }
}

