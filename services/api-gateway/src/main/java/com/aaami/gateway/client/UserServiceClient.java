package com.aaami.gateway.client;

import com.aaami.shared.command.CreateUserCommand;
import com.aaami.shared.command.UpdateUserCommand;
import com.aaami.shared.dto.PaginatedResponse;
import com.aaami.shared.dto.UserDto;
import com.aaami.shared.dto.UserRole;
import com.aaami.gateway.config.ServiceProperties;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserServiceClient {
    
    private final RestTemplate restTemplate;
    private final ServiceProperties serviceProperties;
    
    @CircuitBreaker(name = "userService", fallbackMethod = "createUserFallback")
    @Retry(name = "userService")
    public UserDto createUser(CreateUserCommand command) {
        String url = serviceProperties.getUserServiceUrl() + "/api/users";
        ResponseEntity<UserDto> response = restTemplate.postForEntity(url, command, UserDto.class);
        return response.getBody();
    }
    
    @CircuitBreaker(name = "userService", fallbackMethod = "getAllUsersFallback")
    @Retry(name = "userService")
    public PaginatedResponse<UserDto> getAllUsers(String firstName, String lastName, String email, UserRole role,
                                                   Integer page, Integer size, String sortBy, String sortDirection) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(serviceProperties.getUserServiceUrl() + "/api/users");
        
        if (firstName != null) {
            builder.queryParam("firstName", firstName);
        }
        if (lastName != null) {
            builder.queryParam("lastName", lastName);
        }
        if (email != null) {
            builder.queryParam("email", email);
        }
        if (role != null) {
            builder.queryParam("role", role);
        }
        if (page != null) {
            builder.queryParam("page", page);
        }
        if (size != null) {
            builder.queryParam("size", size);
        }
        if (sortBy != null) {
            builder.queryParam("sortBy", sortBy);
        }
        if (sortDirection != null) {
            builder.queryParam("sortDirection", sortDirection);
        }
        
        ResponseEntity<PaginatedResponse<UserDto>> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PaginatedResponse<UserDto>>() {}
        );
        return response.getBody();
    }
    
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserFallback")
    @Retry(name = "userService")
    public UserDto getUser(Long id) {
        String url = serviceProperties.getUserServiceUrl() + "/api/users/" + id;
        ResponseEntity<UserDto> response = restTemplate.getForEntity(url, UserDto.class);
        return response.getBody();
    }
    
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByEmailFallback")
    @Retry(name = "userService")
    public UserDto getUserByEmail(String email) {
        String url = serviceProperties.getUserServiceUrl() + "/api/users/email/" + email;
        ResponseEntity<UserDto> response = restTemplate.getForEntity(url, UserDto.class);
        return response.getBody();
    }
    
    /**
     * Verifies user password and returns user if valid.
     * 
     * @param email User email
     * @param password User password
     * @return UserDto if password is correct
     * @throws org.springframework.web.client.HttpClientErrorException if password is invalid
     */
    @CircuitBreaker(name = "userService", fallbackMethod = "verifyPasswordFallback")
    @Retry(name = "userService")
    public UserDto verifyPassword(String email, String password) {
        String url = serviceProperties.getUserServiceUrl() + "/api/users/verify-password";
        Map<String, String> request = Map.of("email", email, "password", password);
        ResponseEntity<UserDto> response = restTemplate.postForEntity(url, request, UserDto.class);
        return response.getBody();
    }
    
    @CircuitBreaker(name = "userService", fallbackMethod = "updateUserFallback")
    @Retry(name = "userService")
    public UserDto updateUser(Long id, UpdateUserCommand command) {
        String url = serviceProperties.getUserServiceUrl() + "/api/users/" + id;
        ResponseEntity<UserDto> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                new HttpEntity<>(command),
                UserDto.class
        );
        return response.getBody();
    }
    
    @CircuitBreaker(name = "userService")
    @Retry(name = "userService")
    public void deleteUser(Long id) {
        String url = serviceProperties.getUserServiceUrl() + "/api/users/" + id;
        restTemplate.delete(url);
    }
    
    // Fallback methods
    private UserDto createUserFallback(CreateUserCommand command, Exception ex) {
        throw new RuntimeException("User service unavailable. Unable to create user.", ex);
    }
    
    private PaginatedResponse<UserDto> getAllUsersFallback(String firstName, String lastName, String email, 
                                                            UserRole role, Integer page, Integer size, 
                                                            String sortBy, String sortDirection, Exception ex) {
        throw new RuntimeException("User service unavailable. Unable to get all users.", ex);
    }
    
    private UserDto getUserFallback(Long id, Exception ex) {
        throw new RuntimeException("User service unavailable. Unable to get user: " + id, ex);
    }
    
    private UserDto getUserByEmailFallback(String email, Exception ex) {
        throw new RuntimeException("User service unavailable. Unable to get user by email: " + email, ex);
    }
    
    private UserDto verifyPasswordFallback(String email, String password, Exception ex) {
        throw new RuntimeException("User service unavailable. Unable to verify password.", ex);
    }
    
    private UserDto updateUserFallback(Long id, UpdateUserCommand command, Exception ex) {
        throw new RuntimeException("User service unavailable. Unable to update user: " + id, ex);
    }
}

