package com.aaami.order.handler;

import com.aaami.cqrs.QueryHandler;
import com.aaami.shared.dto.OrderDto;
import com.aaami.shared.dto.PaginatedResponse;
import com.aaami.order.mapper.OrderMapper;
import com.aaami.order.query.GetAllOrdersQuery;
import com.aaami.order.repository.OrderRepository;
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
public class GetAllOrdersQueryHandler implements QueryHandler<GetAllOrdersQuery, PaginatedResponse<OrderDto>> {
    
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    
    @Override
    public PaginatedResponse<OrderDto> handle(GetAllOrdersQuery query) {
        Specification<com.aaami.order.domain.Order> spec = buildSpecification(query);
        
        // Default pagination values
        int page = query.getPage() != null && query.getPage() >= 0 ? query.getPage() : 0;
        int size = query.getSize() != null && query.getSize() > 0 ? query.getSize() : 20;
        
        // Build sorting
        Sort sort = buildSort(query.getSortBy(), query.getSortDirection());
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<com.aaami.order.domain.Order> orderPage = orderRepository.findAll(spec, pageable);
        
        return PaginatedResponse.<OrderDto>builder()
                .content(orderPage.getContent().stream()
                        .map(orderMapper::toDto)
                        .toList())
                .page(orderPage.getNumber())
                .size(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .first(orderPage.isFirst())
                .last(orderPage.isLast())
                .build();
    }
    
    private Sort buildSort(String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "createdAt"); // Default sort by creation date descending
        }
        
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;
        
        return Sort.by(direction, sortBy);
    }
    
    private Specification<com.aaami.order.domain.Order> buildSpecification(GetAllOrdersQuery query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Always exclude deleted orders
            predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));
            
            if (query.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("userId"), query.getUserId()));
            }
            
            if (query.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), query.getStatus()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

