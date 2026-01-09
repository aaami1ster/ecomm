package com.aaami.order.client;

import com.aaami.shared.command.DecreaseProductQuantityCommand;
import com.aaami.shared.dto.ProductDto;
import com.aaami.config.ServiceProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class ProductServiceClient {
    
    private final RestTemplate restTemplate;
    private final ServiceProperties serviceProperties;
    
    public ProductDto getProduct(Long id) {
        String url = serviceProperties.getProductServiceUrl() + "/api/products/" + id;
        ResponseEntity<ProductDto> response = restTemplate.getForEntity(url, ProductDto.class);
        return response.getBody();
    }
    
    public ProductDto decreaseProductQuantity(Long productId, Integer quantity) {
        String url = serviceProperties.getProductServiceUrl() + "/api/products/" + productId + "/decrease-quantity";
        DecreaseProductQuantityCommand command = new DecreaseProductQuantityCommand(productId, quantity);
        ResponseEntity<ProductDto> response = restTemplate.postForEntity(url, command, ProductDto.class);
        return response.getBody();
    }
}

