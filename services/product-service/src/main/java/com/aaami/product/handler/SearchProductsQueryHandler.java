package com.aaami.product.handler;

import com.aaami.cqrs.QueryHandler;
import com.aaami.shared.dto.ProductDto;
import com.aaami.product.mapper.ProductMapper;
import com.aaami.product.query.SearchProductsQuery;
import com.aaami.product.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SearchProductsQueryHandler implements QueryHandler<SearchProductsQuery, List<ProductDto>> {
    
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    
    @Override
    public List<ProductDto> handle(SearchProductsQuery query) {
        Specification<com.aaami.product.domain.Product> spec = buildSpecification(query);
        List<com.aaami.product.domain.Product> products = productRepository.findAll(spec);
        return products.stream()
                .map(productMapper::toDto)
                .toList();
    }
    
    private Specification<com.aaami.product.domain.Product> buildSpecification(SearchProductsQuery query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Always exclude deleted products
            predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));
            
            if (query.getName() != null && !query.getName().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + query.getName().toLowerCase() + "%"
                ));
            }
            
            if (query.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), query.getMinPrice()));
            }
            
            if (query.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), query.getMaxPrice()));
            }
            
            if (query.getAvailableOnly() != null && query.getAvailableOnly()) {
                predicates.add(criteriaBuilder.greaterThan(root.get("quantity"), 0));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

