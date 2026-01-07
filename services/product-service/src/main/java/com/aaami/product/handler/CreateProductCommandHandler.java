package com.aaami.product.handler;

import com.aaami.cqrs.CommandHandler;
import com.aaami.product.command.CreateProductCommand;
import com.aaami.product.domain.Product;
import com.aaami.product.dto.ProductDto;
import com.aaami.product.mapper.ProductMapper;
import com.aaami.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CreateProductCommandHandler implements CommandHandler<CreateProductCommand, ProductDto> {
    
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    
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
        return productMapper.toDto(savedProduct);
    }
}

