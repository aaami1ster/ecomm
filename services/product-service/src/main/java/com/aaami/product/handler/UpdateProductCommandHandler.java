package com.aaami.product.handler;

import com.aaami.cqrs.CommandHandler;
import com.aaami.product.command.UpdateProductCommand;
import com.aaami.product.domain.Product;
import com.aaami.product.dto.ProductDto;
import com.aaami.product.mapper.ProductMapper;
import com.aaami.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UpdateProductCommandHandler implements CommandHandler<UpdateProductCommand, ProductDto> {
    
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    
    @Override
    @Transactional
    public ProductDto handle(UpdateProductCommand command) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(command.getId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + command.getId()));
        
        if (command.getName() != null) {
            product.setName(command.getName());
        }
        if (command.getDescription() != null) {
            product.setDescription(command.getDescription());
        }
        if (command.getPrice() != null) {
            product.setPrice(command.getPrice());
        }
        if (command.getQuantity() != null) {
            product.setQuantity(command.getQuantity());
        }
        
        Product updatedProduct = productRepository.save(product);
        return productMapper.toDto(updatedProduct);
    }
}

