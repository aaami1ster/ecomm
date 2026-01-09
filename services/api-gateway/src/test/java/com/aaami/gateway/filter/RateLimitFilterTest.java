package com.aaami.gateway.filter;

import com.aaami.gateway.service.RateLimitService;
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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitFilter Tests")
class RateLimitFilterTest {

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        lenient().when(rateLimitService.getMaxRequests()).thenReturn(5);
        lenient().when(rateLimitService.getWindowMinutes()).thenReturn(15);
    }

    @Test
    @DisplayName("Should allow request when rate limit not exceeded")
    void doFilterInternal_ShouldAllowRequest_WhenRateLimitNotExceeded() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(rateLimitService.isAllowed("192.168.1.1")).thenReturn(true);
        when(rateLimitService.getRemainingRequests("192.168.1.1")).thenReturn(3);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(response).setHeader("X-RateLimit-Limit", "5");
        verify(response).setHeader("X-RateLimit-Remaining", "3");
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    @DisplayName("Should block request when rate limit exceeded")
    void doFilterInternal_ShouldBlockRequest_WhenRateLimitExceeded() throws Exception {
        // Given
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(rateLimitService.isAllowed("192.168.1.1")).thenReturn(false);
        when(rateLimitService.getRemainingRequests("192.168.1.1")).thenReturn(0);
        when(response.getWriter()).thenReturn(printWriter);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, never()).doFilter(request, response);
        verify(response).setStatus(429); // TOO_MANY_REQUESTS
        verify(response).setContentType("application/json");
        verify(response).setHeader("X-RateLimit-Limit", "5");
        verify(response).setHeader("X-RateLimit-Remaining", "0");
        verify(response).setHeader("Retry-After", "900"); // 15 minutes * 60 seconds
        assertTrue(stringWriter.toString().contains("Rate limit exceeded"));
    }

    @Test
    @DisplayName("Should not apply rate limiting to non-login endpoints")
    void doFilterInternal_ShouldNotApplyRateLimit_ForNonLoginEndpoints() throws Exception {
        // Given
        lenient().when(request.getRequestURI()).thenReturn("/api/products");
        lenient().when(request.getMethod()).thenReturn("GET");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(rateLimitService, never()).isAllowed(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not apply rate limiting to login GET requests")
    void doFilterInternal_ShouldNotApplyRateLimit_ForLoginGetRequest() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getMethod()).thenReturn("GET");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(rateLimitService, never()).isAllowed(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should extract IP from X-Forwarded-For header")
    void getClientIpAddress_ShouldUseXForwardedFor_WhenPresent() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 192.168.1.1");
        when(rateLimitService.isAllowed("10.0.0.1")).thenReturn(true);
        when(rateLimitService.getRemainingRequests("10.0.0.1")).thenReturn(3);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(rateLimitService).isAllowed("10.0.0.1");
    }

    @Test
    @DisplayName("Should extract IP from X-Real-IP header")
    void getClientIpAddress_ShouldUseXRealIP_WhenPresent() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn("10.0.0.2");
        when(rateLimitService.isAllowed("10.0.0.2")).thenReturn(true);
        when(rateLimitService.getRemainingRequests("10.0.0.2")).thenReturn(3);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(rateLimitService).isAllowed("10.0.0.2");
    }

    @Test
    @DisplayName("Should use remote address when no proxy headers")
    void getClientIpAddress_ShouldUseRemoteAddr_WhenNoProxyHeaders() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");
        when(rateLimitService.isAllowed("192.168.1.100")).thenReturn(true);
        when(rateLimitService.getRemainingRequests("192.168.1.100")).thenReturn(3);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(rateLimitService).isAllowed("192.168.1.100");
    }

    @Test
    @DisplayName("Should handle IOException when writing response")
    void doFilterInternal_ShouldHandleIOException_WhenWritingResponse() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(rateLimitService.isAllowed("192.168.1.1")).thenReturn(false);
        when(rateLimitService.getRemainingRequests("192.168.1.1")).thenReturn(0);
        when(response.getWriter()).thenThrow(new IOException("Write error"));

        // When & Then - should not throw
        assertDoesNotThrow(() -> filter.doFilterInternal(request, response, filterChain));
        verify(filterChain, never()).doFilter(request, response);
    }
}

