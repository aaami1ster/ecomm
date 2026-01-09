package com.aaami.product.handler;

import com.aaami.cqrs.CommandHandler;
import com.aaami.shared.command.CreateProductCommand;
import com.aaami.product.domain.Product;
import com.aaami.shared.dto.ProductDto;
import com.aaami.product.mapper.ProductMapper;
import com.aaami.product.repository.ProductRepository;
import com.aaami.product.service.ProductCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CreateProductCommandHandler implements CommandHandler<CreateProductCommand, ProductDto> {
    
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductCacheService productCacheService;
    
    @Override
    @Transactional
    public ProductDto handle(CreateProductCommand command) {
        Product product = Product.builder()
                .name(command.getName())
                .description(command.getDescription())
                .price(command.getPrice())
                .quantity(command.getQuantity())
                .build();
        
        Product savedProduct = productRepository.save(product);
        ProductDto productDto = productMapper.toDto(savedProduct);
        
        // Cache the newly created product
        productCacheService.cacheProduct(productDto);
        
        return productDto;
    }
}

