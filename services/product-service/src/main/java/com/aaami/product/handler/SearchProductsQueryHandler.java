package com.aaami.product.handler;

import com.aaami.cqrs.QueryHandler;
import com.aaami.shared.dto.PaginatedResponse;
import com.aaami.shared.dto.ProductDto;
import com.aaami.product.mapper.ProductMapper;
import com.aaami.product.query.SearchProductsQuery;
import com.aaami.product.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SearchProductsQueryHandler implements QueryHandler<SearchProductsQuery, PaginatedResponse<ProductDto>> {
    
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    
    @Override
    public PaginatedResponse<ProductDto> handle(SearchProductsQuery query) {
        Specification<com.aaami.product.domain.Product> spec = buildSpecification(query);
        
        // Default pagination values
        int page = query.getPage() != null && query.getPage() >= 0 ? query.getPage() : 0;
        int size = query.getSize() != null && query.getSize() > 0 ? query.getSize() : 20;
        
        // Build sorting
        Sort sort = buildSort(query.getSortBy(), query.getSortDirection());
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<com.aaami.product.domain.Product> productPage = productRepository.findAll(spec, pageable);
        
        return PaginatedResponse.<ProductDto>builder()
                .content(productPage.getContent().stream()
                        .map(productMapper::toDto)
                        .toList())
                .page(productPage.getNumber())
                .size(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .first(productPage.isFirst())
                .last(productPage.isLast())
                .build();
    }
    
    private Sort buildSort(String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.isEmpty()) {
            return Sort.by(Sort.Direction.ASC, "id"); // Default sort
        }
        
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;
        
        return Sort.by(direction, sortBy);
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

