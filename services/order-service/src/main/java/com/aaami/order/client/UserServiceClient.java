package com.aaami.order.client;

import com.aaami.shared.dto.UserDto;
import com.aaami.config.ServiceProperties;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class UserServiceClient {
    
    private final RestTemplate restTemplate;
    private final ServiceProperties serviceProperties;
    
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserFallback")
    @Retry(name = "userService")
    public UserDto getUser(Long id) {
        String url = serviceProperties.getUserServiceUrl() + "/api/users/" + id;
        ResponseEntity<UserDto> response = restTemplate.getForEntity(url, UserDto.class);
        return response.getBody();
    }
    
    // Fallback method
    private UserDto getUserFallback(Long id, Exception ex) {
        throw new RuntimeException("User service unavailable. Unable to get user: " + id, ex);
    }
}

