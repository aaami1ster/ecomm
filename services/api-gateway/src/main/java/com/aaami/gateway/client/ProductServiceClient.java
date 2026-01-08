package com.aaami.gateway.client;

import com.aaami.shared.command.CreateProductCommand;
import com.aaami.shared.command.DeleteProductCommand;
import com.aaami.shared.command.UpdateProductCommand;
import com.aaami.shared.dto.PaginatedResponse;
import com.aaami.shared.dto.ProductDto;
import com.aaami.gateway.config.ServiceProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class ProductServiceClient {
    
    private final RestTemplate restTemplate;
    private final ServiceProperties serviceProperties;
    
    public ProductDto createProduct(CreateProductCommand command) {
        String url = serviceProperties.getProductServiceUrl() + "/api/products";
        ResponseEntity<ProductDto> response = restTemplate.postForEntity(url, command, ProductDto.class);
        return response.getBody();
    }
    
    public ProductDto getProduct(Long id) {
        String url = serviceProperties.getProductServiceUrl() + "/api/products/" + id;
        ResponseEntity<ProductDto> response = restTemplate.getForEntity(url, ProductDto.class);
        return response.getBody();
    }
    
    public PaginatedResponse<ProductDto> searchProducts(String name, BigDecimal minPrice, BigDecimal maxPrice, Boolean availableOnly,
                                                         Integer page, Integer size, String sortBy, String sortDirection) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(serviceProperties.getProductServiceUrl() + "/api/products");
        
        if (name != null) {
            builder.queryParam("name", name);
        }
        if (minPrice != null) {
            builder.queryParam("minPrice", minPrice);
        }
        if (maxPrice != null) {
            builder.queryParam("maxPrice", maxPrice);
        }
        if (availableOnly != null) {
            builder.queryParam("availableOnly", availableOnly);
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
        
        ResponseEntity<PaginatedResponse<ProductDto>> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PaginatedResponse<ProductDto>>() {}
        );
        
        return response.getBody();
    }
    
    public ProductDto updateProduct(Long id, UpdateProductCommand command) {
        String url = serviceProperties.getProductServiceUrl() + "/api/products/" + id;
        ResponseEntity<ProductDto> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                new HttpEntity<>(command),
                ProductDto.class
        );
        return response.getBody();
    }
    
    public void deleteProduct(Long id) {
        String url = serviceProperties.getProductServiceUrl() + "/api/products/" + id;
        restTemplate.delete(url);
    }
}

