package com.aaami.product.handler;

import com.aaami.cqrs.QueryHandler;
import com.aaami.shared.dto.ProductDto;
import com.aaami.product.mapper.ProductMapper;
import com.aaami.product.query.GetProductQuery;
import com.aaami.product.repository.ProductRepository;
import com.aaami.product.service.ProductCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Query handler for getting a single product by ID.
 * Implements cache-aside pattern:
 * 1. Check Redis cache first
 * 2. If cache miss, load from database
 * 3. Store in cache for future requests
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GetProductQueryHandler implements QueryHandler<GetProductQuery, ProductDto> {
    
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductCacheService productCacheService;
    
    @Override
    public ProductDto handle(GetProductQuery query) {
        Long productId = query.getId();
        
        // Try cache first (cache-aside pattern)
        ProductDto cachedProduct = productCacheService.getCachedProduct(productId);
        if (cachedProduct != null) {
            return cachedProduct;
        }
        
        // Cache miss - load from database
        ProductDto product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .map(productMapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));
        
        // Store in cache for future requests
        productCacheService.cacheProduct(product);
        
        return product;
    }
}

