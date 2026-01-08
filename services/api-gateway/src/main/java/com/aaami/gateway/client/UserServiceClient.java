package com.aaami.gateway.client;

import com.aaami.shared.command.CreateUserCommand;
import com.aaami.shared.command.UpdateUserCommand;
import com.aaami.shared.dto.PaginatedResponse;
import com.aaami.shared.dto.UserDto;
import com.aaami.shared.dto.UserRole;
import com.aaami.gateway.config.ServiceProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class UserServiceClient {
    
    private final RestTemplate restTemplate;
    private final ServiceProperties serviceProperties;
    
    public UserDto createUser(CreateUserCommand command) {
        String url = serviceProperties.getUserServiceUrl() + "/api/users";
        ResponseEntity<UserDto> response = restTemplate.postForEntity(url, command, UserDto.class);
        return response.getBody();
    }
    
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
    
    public UserDto getUser(Long id) {
        String url = serviceProperties.getUserServiceUrl() + "/api/users/" + id;
        ResponseEntity<UserDto> response = restTemplate.getForEntity(url, UserDto.class);
        return response.getBody();
    }
    
    public UserDto getUserByEmail(String email) {
        String url = serviceProperties.getUserServiceUrl() + "/api/users/email/" + email;
        ResponseEntity<UserDto> response = restTemplate.getForEntity(url, UserDto.class);
        return response.getBody();
    }
    
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
    
    public void deleteUser(Long id) {
        String url = serviceProperties.getUserServiceUrl() + "/api/users/" + id;
        restTemplate.delete(url);
    }
}

