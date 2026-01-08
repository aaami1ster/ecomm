package com.aaami.order.handler;

import com.aaami.cqrs.QueryHandler;
import com.aaami.shared.dto.OrderDto;
import com.aaami.order.mapper.OrderMapper;
import com.aaami.order.query.GetAllOrdersQuery;
import com.aaami.order.repository.OrderRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GetAllOrdersQueryHandler implements QueryHandler<GetAllOrdersQuery, List<OrderDto>> {
    
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    
    @Override
    public List<OrderDto> handle(GetAllOrdersQuery query) {
        Specification<com.aaami.order.domain.Order> spec = buildSpecification(query);
        List<com.aaami.order.domain.Order> orders = orderRepository.findAll(spec);
        return orders.stream()
                .map(orderMapper::toDto)
                .toList();
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

