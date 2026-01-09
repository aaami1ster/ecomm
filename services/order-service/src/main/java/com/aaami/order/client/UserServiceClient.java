package com.aaami.order.client;

import com.aaami.shared.dto.UserDto;
import com.aaami.config.ServiceProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class UserServiceClient {
    
    private final RestTemplate restTemplate;
    private final ServiceProperties serviceProperties;
    
    public UserDto getUser(Long id) {
        String url = serviceProperties.getUserServiceUrl() + "/api/users/" + id;
        ResponseEntity<UserDto> response = restTemplate.getForEntity(url, UserDto.class);
        return response.getBody();
    }
}

