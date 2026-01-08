package com.aaami.product.handler;

import com.aaami.cqrs.QueryHandler;
import com.aaami.shared.dto.ProductDto;
import com.aaami.product.mapper.ProductMapper;
import com.aaami.product.query.GetProductQuery;
import com.aaami.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetProductQueryHandler implements QueryHandler<GetProductQuery, ProductDto> {
    
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    
    @Override
    public ProductDto handle(GetProductQuery query) {
        return productRepository.findByIdAndDeletedAtIsNull(query.getId())
                .map(productMapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + query.getId()));
    }
}

