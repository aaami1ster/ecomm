package com.aaami.order.handler;

import com.aaami.cqrs.CommandHandler;
import com.aaami.shared.command.UpdateOrderCommand;
import com.aaami.order.domain.Order;
import com.aaami.shared.dto.OrderDto;
import com.aaami.shared.dto.OrderStatus;
import com.aaami.order.mapper.OrderMapper;
import com.aaami.order.repository.OrderRepository;
import com.aaami.order.service.OrderEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateOrderCommandHandler implements CommandHandler<UpdateOrderCommand, OrderDto> {
    
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderEventProducer eventProducer;
    
    @Override
    @Transactional
    public OrderDto handle(UpdateOrderCommand command) {
        Order order = orderRepository.findByIdAndDeletedAtIsNull(command.getId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + command.getId()));
        
        OrderStatus previousStatus = order.getStatus();
        
        if (command.getStatus() != null) {
            validateStatusTransition(previousStatus, command.getStatus());
            order.setStatus(command.getStatus());
            log.info("Order {} status updated to {}", order.getId(), command.getStatus());
        }
        
        Order updatedOrder = orderRepository.save(order);
        OrderDto orderDto = orderMapper.toDto(updatedOrder);
        
        // Publish event if status changed
        if (command.getStatus() != null && !previousStatus.equals(command.getStatus())) {
            eventProducer.publishOrderStatusChanged(orderDto, previousStatus, command.getStatus());
        }
        
        return orderDto;
    }
    
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (newStatus == OrderStatus.CONFIRMED) {
            // Only PENDING orders can be confirmed
            if (currentStatus != OrderStatus.PENDING) {
                throw new IllegalStateException(
                    String.format("Order cannot be confirmed. Current status: %s. Only PENDING orders can be confirmed.",
                        currentStatus));
            }
        } else if (newStatus == OrderStatus.CANCELLED) {
            // Only PENDING or CONFIRMED orders can be cancelled
            if (currentStatus != OrderStatus.PENDING && currentStatus != OrderStatus.CONFIRMED) {
                throw new IllegalStateException(
                    String.format("Order cannot be cancelled. Current status: %s. Only PENDING or CONFIRMED orders can be cancelled.",
                        currentStatus));
            }
        }
        // Allow other status transitions (e.g., CONFIRMED -> COMPLETED) without restrictions
    }
}

