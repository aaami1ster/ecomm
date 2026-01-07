package com.aaami.product.handler;

import com.aaami.cqrs.QueryHandler;
import com.aaami.product.dto.ProductDto;
import com.aaami.product.mapper.ProductMapper;
import com.aaami.product.query.SearchProductsQuery;
import com.aaami.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SearchProductsQueryHandler implements QueryHandler<SearchProductsQuery, List<ProductDto>> {
    
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    
    @Override
    public List<ProductDto> handle(SearchProductsQuery query) {
        List<com.aaami.product.domain.Product> products = productRepository.searchProducts(
                query.getName(),
                query.getMinPrice(),
                query.getMaxPrice(),
                query.getAvailableOnly()
        );
        
        return products.stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }
}

