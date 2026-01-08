package com.aaami.order.handler;

import com.aaami.cqrs.CommandHandler;
import com.aaami.shared.command.DeleteOrderCommand;
import com.aaami.order.domain.Order;
import com.aaami.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DeleteOrderCommandHandler implements CommandHandler<DeleteOrderCommand, Void> {
    
    private final OrderRepository orderRepository;
    
    @Override
    @Transactional
    public Void handle(DeleteOrderCommand command) {
        Order order = orderRepository.findByIdAndDeletedAtIsNull(command.getId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + command.getId()));
        
        order.setDeletedAt(LocalDateTime.now());
        orderRepository.save(order);
        return null;
    }
}

