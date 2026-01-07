package com.aaami.order.handler;

import com.aaami.cqrs.QueryHandler;
import com.aaami.order.dto.OrderDto;
import com.aaami.order.mapper.OrderMapper;
import com.aaami.order.query.GetOrderQuery;
import com.aaami.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetOrderQueryHandler implements QueryHandler<GetOrderQuery, OrderDto> {
    
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    
    @Override
    public OrderDto handle(GetOrderQuery query) {
        return orderRepository.findById(query.getId())
                .map(orderMapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + query.getId()));
    }
}

