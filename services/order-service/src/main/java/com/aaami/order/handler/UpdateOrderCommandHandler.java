package com.aaami.order.handler;

import com.aaami.cqrs.CommandHandler;
import com.aaami.shared.command.UpdateOrderCommand;
import com.aaami.order.domain.Order;
import com.aaami.shared.dto.OrderDto;
import com.aaami.order.mapper.OrderMapper;
import com.aaami.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UpdateOrderCommandHandler implements CommandHandler<UpdateOrderCommand, OrderDto> {
    
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    
    @Override
    @Transactional
    public OrderDto handle(UpdateOrderCommand command) {
        Order order = orderRepository.findByIdAndDeletedAtIsNull(command.getId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + command.getId()));
        
        if (command.getStatus() != null) {
            order.setStatus(command.getStatus());
        }
        
        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toDto(updatedOrder);
    }
}

