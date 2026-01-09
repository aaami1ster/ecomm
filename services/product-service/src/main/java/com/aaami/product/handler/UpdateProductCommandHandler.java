package com.aaami.product.handler;

import com.aaami.cqrs.CommandHandler;
import com.aaami.shared.command.UpdateProductCommand;
import com.aaami.product.domain.Product;
import com.aaami.shared.dto.ProductDto;
import com.aaami.product.mapper.ProductMapper;
import com.aaami.product.repository.ProductRepository;
import com.aaami.product.service.ProductCacheService;
import com.aaami.product.service.ProductEventProducer;
import com.aaami.product.exception.DuplicateProductNameException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UpdateProductCommandHandler implements CommandHandler<UpdateProductCommand, ProductDto> {
    
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductCacheService productCacheService;
    private final ProductEventProducer eventProducer;
    
    @Override
    @Transactional
    public ProductDto handle(UpdateProductCommand command) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(command.getId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + command.getId()));
        
        if (command.getName() != null && !command.getName().equals(product.getName())) {
            // Check if the new name already exists (excluding the current product)
            if (productRepository.existsByNameAndDeletedAtIsNull(command.getName())) {
                throw new DuplicateProductNameException("Product with name '" + command.getName() + "' already exists");
            }
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
        ProductDto productDto = productMapper.toDto(updatedProduct);
        
        // Invalidate cache and update with new data
        productCacheService.invalidateProduct(command.getId());
        productCacheService.cacheProduct(productDto);
        
        // Publish event after successful update
        eventProducer.publishProductUpdated(productDto);
        
        return productDto;
    }
}

