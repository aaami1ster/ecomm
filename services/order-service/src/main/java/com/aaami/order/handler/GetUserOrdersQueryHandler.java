package com.aaami.order.handler;

import com.aaami.cqrs.QueryHandler;
import com.aaami.shared.dto.OrderDto;
import com.aaami.order.mapper.OrderMapper;
import com.aaami.order.query.GetUserOrdersQuery;
import com.aaami.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GetUserOrdersQueryHandler implements QueryHandler<GetUserOrdersQuery, List<OrderDto>> {
    
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    
    @Override
    public List<OrderDto> handle(GetUserOrdersQuery query) {
        return orderRepository.findByUserId(query.getUserId()).stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }
}

