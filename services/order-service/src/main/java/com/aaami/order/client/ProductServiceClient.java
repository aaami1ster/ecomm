package com.aaami.order.client;

import com.aaami.shared.command.DecreaseProductQuantityCommand;
import com.aaami.shared.dto.ProductDto;
import com.aaami.config.ServiceProperties;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class ProductServiceClient {
    
    private final RestTemplate restTemplate;
    private final ServiceProperties serviceProperties;
    
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductFallback")
    @Retry(name = "productService")
    public ProductDto getProduct(Long id) {
        String url = serviceProperties.getProductServiceUrl() + "/api/products/" + id;
        ResponseEntity<ProductDto> response = restTemplate.getForEntity(url, ProductDto.class);
        return response.getBody();
    }
    
    @CircuitBreaker(name = "productService", fallbackMethod = "decreaseProductQuantityFallback")
    @Retry(name = "productService")
    public ProductDto decreaseProductQuantity(Long productId, Integer quantity) {
        String url = serviceProperties.getProductServiceUrl() + "/api/products/" + productId + "/decrease-quantity";
        DecreaseProductQuantityCommand command = new DecreaseProductQuantityCommand(productId, quantity);
        ResponseEntity<ProductDto> response = restTemplate.postForEntity(url, command, ProductDto.class);
        return response.getBody();
    }
    
    // Fallback methods
    private ProductDto getProductFallback(Long id, Exception ex) {
        // Check if the original exception was a 404 (product not found)
        if (ex instanceof org.springframework.web.client.HttpClientErrorException.NotFound) {
            throw new IllegalArgumentException("Product not found with id: " + id, ex);
        }
        // For other errors (circuit breaker open, service unavailable, etc.)
        throw new IllegalStateException("Product service unavailable. Unable to get product: " + id, ex);
    }
    
    private ProductDto decreaseProductQuantityFallback(Long productId, Integer quantity, Exception ex) {
        // Check if the original exception was a 404 (product not found)
        if (ex instanceof org.springframework.web.client.HttpClientErrorException.NotFound) {
            throw new IllegalArgumentException("Product not found with id: " + productId, ex);
        }
        // For other errors (circuit breaker open, service unavailable, etc.)
        throw new IllegalStateException("Product service unavailable. Unable to decrease product quantity: " + productId, ex);
    }
}

