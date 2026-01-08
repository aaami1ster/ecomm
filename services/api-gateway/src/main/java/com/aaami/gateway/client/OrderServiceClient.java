package com.aaami.gateway.client;

import com.aaami.shared.command.CreateOrderCommand;
import com.aaami.shared.dto.OrderDto;
import com.aaami.gateway.config.ServiceProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderServiceClient {
    
    private final RestTemplate restTemplate;
    private final ServiceProperties serviceProperties;
    
    public OrderDto createOrder(CreateOrderCommand command) {
        String url = serviceProperties.getOrderServiceUrl() + "/api/orders";
        ResponseEntity<OrderDto> response = restTemplate.postForEntity(url, command, OrderDto.class);
        return response.getBody();
    }
    
    public OrderDto getOrder(Long id) {
        String url = serviceProperties.getOrderServiceUrl() + "/api/orders/" + id;
        ResponseEntity<OrderDto> response = restTemplate.getForEntity(url, OrderDto.class);
        return response.getBody();
    }
    
    public List<OrderDto> getUserOrders(Long userId) {
        String url = serviceProperties.getOrderServiceUrl() + "/api/orders/user/" + userId;
        ResponseEntity<List<OrderDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<OrderDto>>() {}
        );
        return response.getBody();
    }
}

