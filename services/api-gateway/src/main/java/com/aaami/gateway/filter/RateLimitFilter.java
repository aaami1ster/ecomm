package com.aaami.gateway.filter;

import com.aaami.gateway.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter to apply rate limiting to specific endpoints (e.g., login).
 * 
 * Uses client IP address and/or email as the rate limit key.
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {
    
    private final RateLimitService rateLimitService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Apply rate limiting only to login endpoint
        if (request.getRequestURI().equals("/api/auth/login") && 
            "POST".equalsIgnoreCase(request.getMethod())) {
            
            // Use IP address as rate limit key
            String clientIp = getClientIpAddress(request);
            
            // Optionally, also use email if available in request body
            // For simplicity, we'll use IP address only
            // In production, you might want to use email for more granular control
            
            if (!rateLimitService.isAllowed(clientIp)) {
                int remaining = rateLimitService.getRemainingRequests(clientIp);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.setHeader("X-RateLimit-Limit", String.valueOf(rateLimitService.getMaxRequests()));
                response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
                response.setHeader("Retry-After", String.valueOf(rateLimitService.getWindowMinutes() * 60));
                
                try {
                    response.getWriter().write(
                        String.format("{\"error\":\"Rate limit exceeded. Please try again after %d minutes.\"}", 
                            rateLimitService.getWindowMinutes())
                    );
                } catch (IOException e) {
                    log.error("Error writing rate limit response", e);
                }
                log.warn("Rate limit exceeded for IP: {}", clientIp);
                return;
            }
            
            // Add rate limit headers to successful requests
            int remaining = rateLimitService.getRemainingRequests(clientIp);
            response.setHeader("X-RateLimit-Limit", String.valueOf(rateLimitService.getMaxRequests()));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Extracts the client IP address from the request.
     * Handles proxies and load balancers by checking X-Forwarded-For header.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}

