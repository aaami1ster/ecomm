package com.aaami.product.handler;

import com.aaami.cqrs.CommandHandler;
import com.aaami.shared.command.DeleteProductCommand;
import com.aaami.shared.dto.ProductDto;
import com.aaami.product.domain.Product;
import com.aaami.product.mapper.ProductMapper;
import com.aaami.product.repository.ProductRepository;
import com.aaami.product.service.ProductCacheService;
import com.aaami.product.service.ProductEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DeleteProductCommandHandler implements CommandHandler<DeleteProductCommand, Void> {
    
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductCacheService productCacheService;
    private final ProductEventProducer eventProducer;
    
    @Override
    @Transactional
    public Void handle(DeleteProductCommand command) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(command.getId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + command.getId()));
        
        // Get product DTO before deletion for event
        ProductDto productDto = productMapper.toDto(product);
        
        product.setDeletedAt(LocalDateTime.now());
        productRepository.save(product);
        
        // Invalidate cache after deletion
        productCacheService.invalidateProduct(command.getId());
        
        // Publish event after successful deletion
        eventProducer.publishProductDeleted(productDto);
        
        return null;
    }
}

