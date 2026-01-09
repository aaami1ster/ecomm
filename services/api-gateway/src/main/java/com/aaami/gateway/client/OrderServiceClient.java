package com.aaami.gateway.client;

import com.aaami.shared.command.CreateOrderCommand;
import com.aaami.shared.command.UpdateOrderCommand;
import com.aaami.shared.dto.OrderDto;
import com.aaami.shared.dto.OrderStatus;
import com.aaami.shared.dto.PaginatedResponse;
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

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderServiceClient {
    
    private final RestTemplate restTemplate;
    private final ServiceProperties serviceProperties;
    
    @CircuitBreaker(name = "orderService", fallbackMethod = "createOrderFallback")
    @Retry(name = "orderService")
    public OrderDto createOrder(CreateOrderCommand command) {
        String url = serviceProperties.getOrderServiceUrl() + "/api/orders";
        ResponseEntity<OrderDto> response = restTemplate.postForEntity(url, command, OrderDto.class);
        return response.getBody();
    }
    
    @CircuitBreaker(name = "orderService", fallbackMethod = "getAllOrdersFallback")
    @Retry(name = "orderService")
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
    
    @CircuitBreaker(name = "orderService", fallbackMethod = "getOrderFallback")
    @Retry(name = "orderService")
    public OrderDto getOrder(Long id) {
        String url = serviceProperties.getOrderServiceUrl() + "/api/orders/" + id;
        ResponseEntity<OrderDto> response = restTemplate.getForEntity(url, OrderDto.class);
        return response.getBody();
    }
    
    @CircuitBreaker(name = "orderService", fallbackMethod = "getUserOrdersFallback")
    @Retry(name = "orderService")
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
    
    @CircuitBreaker(name = "orderService", fallbackMethod = "updateOrderFallback")
    @Retry(name = "orderService")
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
    
    @CircuitBreaker(name = "orderService")
    @Retry(name = "orderService")
    public void deleteOrder(Long id) {
        String url = serviceProperties.getOrderServiceUrl() + "/api/orders/" + id;
        restTemplate.delete(url);
    }
    
    // Fallback methods
    private OrderDto createOrderFallback(CreateOrderCommand command, Exception ex) {
        throw new RuntimeException("Order service unavailable. Unable to create order.", ex);
    }
    
    private PaginatedResponse<OrderDto> getAllOrdersFallback(Long userId, OrderStatus status, 
                                                             Integer page, Integer size, 
                                                             String sortBy, String sortDirection, Exception ex) {
        throw new RuntimeException("Order service unavailable. Unable to get all orders.", ex);
    }
    
    private OrderDto getOrderFallback(Long id, Exception ex) {
        throw new RuntimeException("Order service unavailable. Unable to get order: " + id, ex);
    }
    
    private List<OrderDto> getUserOrdersFallback(Long userId, Exception ex) {
        throw new RuntimeException("Order service unavailable. Unable to get user orders: " + userId, ex);
    }
    
    private OrderDto updateOrderFallback(Long id, UpdateOrderCommand command, Exception ex) {
        throw new RuntimeException("Order service unavailable. Unable to update order: " + id, ex);
    }
}

