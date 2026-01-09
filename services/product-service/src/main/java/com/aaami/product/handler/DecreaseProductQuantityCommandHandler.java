package com.aaami.product.handler;

import com.aaami.cqrs.CommandHandler;
import com.aaami.shared.command.DecreaseProductQuantityCommand;
import com.aaami.product.domain.Product;
import com.aaami.shared.dto.ProductDto;
import com.aaami.product.mapper.ProductMapper;
import com.aaami.product.repository.ProductRepository;
import com.aaami.product.service.ProductCacheService;
import com.aaami.product.service.ProductEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DecreaseProductQuantityCommandHandler implements CommandHandler<DecreaseProductQuantityCommand, ProductDto> {
    
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductCacheService productCacheService;
    private final ProductEventProducer eventProducer;
    
    @Override
    @Transactional
    public ProductDto handle(DecreaseProductQuantityCommand command) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(command.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + command.getProductId()));
        
        int currentQuantity = product.getQuantity();
        int quantityToDecrease = command.getQuantity();
        
        if (currentQuantity < quantityToDecrease) {
            throw new IllegalArgumentException(
                String.format("Insufficient stock for product %d. Available: %d, Requested: %d",
                    product.getId(), currentQuantity, quantityToDecrease));
        }
        
        int newQuantity = currentQuantity - quantityToDecrease;
        product.setQuantity(newQuantity);
        
        Product updatedProduct = productRepository.save(product);
        ProductDto productDto = productMapper.toDto(updatedProduct);
        
        log.info("Decreased quantity for product {} by {}. New quantity: {}", 
            product.getId(), quantityToDecrease, newQuantity);
        
        // Invalidate cache and update with new data
        productCacheService.invalidateProduct(command.getProductId());
        productCacheService.cacheProduct(productDto);
        
        // Publish event after successful inventory decrease
        eventProducer.publishInventoryDecreased(productDto, quantityToDecrease, newQuantity);
        
        return productDto;
    }
}

