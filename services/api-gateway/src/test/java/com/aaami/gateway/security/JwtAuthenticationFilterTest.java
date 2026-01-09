package com.aaami.gateway.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Tests")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should set authentication when valid JWT token is provided")
    void doFilterInternal_ShouldSetAuthentication_WhenValidToken() throws Exception {
        // Given
        String jwt = "valid.jwt.token";
        String bearerToken = "Bearer " + jwt;
        
        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(tokenProvider.validateToken(jwt)).thenReturn(true);
        when(tokenProvider.getUserIdFromToken(jwt)).thenReturn(1L);
        when(tokenProvider.getEmailFromToken(jwt)).thenReturn("user@example.com");
        when(tokenProvider.getRoleFromToken(jwt)).thenReturn(com.aaami.shared.dto.UserRole.USER);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(tokenProvider).validateToken(jwt);
        verify(tokenProvider).getUserIdFromToken(jwt);
        verify(tokenProvider).getEmailFromToken(jwt);
        verify(tokenProvider).getRoleFromToken(jwt);
        verify(filterChain).doFilter(request, response);
        
        // Verify authentication was set
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(1L, authentication.getPrincipal());
        assertEquals("user@example.com", authentication.getCredentials());
        assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    @DisplayName("Should not set authentication when no token is provided")
    void doFilterInternal_ShouldNotSetAuthentication_WhenNoToken() throws Exception {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(tokenProvider, never()).validateToken(anyString());
        verify(filterChain).doFilter(request, response);
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
    }

    @Test
    @DisplayName("Should not set authentication when token is invalid")
    void doFilterInternal_ShouldNotSetAuthentication_WhenInvalidToken() throws Exception {
        // Given
        String jwt = "invalid.jwt.token";
        String bearerToken = "Bearer " + jwt;
        
        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(tokenProvider.validateToken(jwt)).thenReturn(false);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(tokenProvider).validateToken(jwt);
        verify(tokenProvider, never()).getUserIdFromToken(anyString());
        verify(filterChain).doFilter(request, response);
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
    }

    @Test
    @DisplayName("Should not set authentication when token doesn't start with Bearer")
    void doFilterInternal_ShouldNotSetAuthentication_WhenTokenNotBearer() throws Exception {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Token some-token");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(tokenProvider, never()).validateToken(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should handle exception gracefully and continue filter chain")
    void doFilterInternal_ShouldHandleException_AndContinueFilterChain() throws Exception {
        // Given
        String jwt = "valid.jwt.token";
        String bearerToken = "Bearer " + jwt;
        
        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(tokenProvider.validateToken(jwt)).thenThrow(new RuntimeException("Token validation error"));

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        // Should not throw exception
    }

    @Test
    @DisplayName("Should extract JWT from Bearer token correctly")
    void getJwtFromRequest_ShouldExtractToken_FromBearerHeader() {
        // Given
        String jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dozjgNryP4J3jVmNHl0w5N_XgL0n3I9PlFUP0THsR8U";
        String bearerToken = "Bearer " + jwt;
        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(tokenProvider.validateToken(jwt)).thenReturn(true);
        when(tokenProvider.getUserIdFromToken(jwt)).thenReturn(1L);
        when(tokenProvider.getEmailFromToken(jwt)).thenReturn("user@example.com");
        when(tokenProvider.getRoleFromToken(jwt)).thenReturn(com.aaami.shared.dto.UserRole.USER);

        // When
        try {
            filter.doFilterInternal(request, response, filterChain);
        } catch (Exception e) {
            fail("Should not throw exception");
        }

        // Then
        verify(tokenProvider).validateToken(jwt);
    }

    @Test
    @DisplayName("Should handle ADMIN role correctly")
    void doFilterInternal_ShouldSetAdminRole_WhenTokenHasAdminRole() throws Exception {
        // Given
        String jwt = "admin.jwt.token";
        String bearerToken = "Bearer " + jwt;
        
        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(tokenProvider.validateToken(jwt)).thenReturn(true);
        when(tokenProvider.getUserIdFromToken(jwt)).thenReturn(1L);
        when(tokenProvider.getEmailFromToken(jwt)).thenReturn("admin@example.com");
        when(tokenProvider.getRoleFromToken(jwt)).thenReturn(com.aaami.shared.dto.UserRole.ADMIN);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}

