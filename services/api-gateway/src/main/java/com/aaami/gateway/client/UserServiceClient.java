package com.aaami.gateway.client;

import com.aaami.shared.command.CreateUserCommand;
import com.aaami.shared.command.UpdateUserCommand;
import com.aaami.shared.dto.UserDto;
import com.aaami.gateway.config.ServiceProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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
}

