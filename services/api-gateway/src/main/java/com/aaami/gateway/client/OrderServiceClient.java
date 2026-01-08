package com.aaami.gateway.client;

import com.aaami.shared.command.CreateOrderCommand;
import com.aaami.shared.command.UpdateOrderCommand;
import com.aaami.shared.dto.OrderDto;
import com.aaami.shared.dto.OrderStatus;
import com.aaami.shared.dto.PaginatedResponse;
import com.aaami.gateway.config.ServiceProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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
    
    public PaginatedResponse<OrderDto> getAllOrders(Long userId, OrderStatus status,
                                                     Integer page, Integer size, String sortBy, String sortDirection) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(serviceProperties.getOrderServiceUrl() + "/api/orders");
        
        if (userId != null) {
            builder.queryParam("userId", userId);
        }
        if (status != null) {
            builder.queryParam("status", status);
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
        
        ResponseEntity<PaginatedResponse<OrderDto>> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PaginatedResponse<OrderDto>>() {}
        );
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
    
    public OrderDto updateOrder(Long id, UpdateOrderCommand command) {
        String url = serviceProperties.getOrderServiceUrl() + "/api/orders/" + id;
        ResponseEntity<OrderDto> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                new HttpEntity<>(command),
                OrderDto.class
        );
        return response.getBody();
    }
    
    public void deleteOrder(Long id) {
        String url = serviceProperties.getOrderServiceUrl() + "/api/orders/" + id;
        restTemplate.delete(url);
    }
}

