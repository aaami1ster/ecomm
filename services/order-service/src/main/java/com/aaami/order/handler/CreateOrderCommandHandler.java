package com.aaami.order.handler;

import com.aaami.cqrs.CommandHandler;
import com.aaami.shared.command.CreateOrderCommand;
import com.aaami.shared.command.OrderItemCommand;
import com.aaami.order.domain.Order;
import com.aaami.order.domain.OrderItem;
import com.aaami.shared.dto.OrderDto;
import com.aaami.order.mapper.OrderMapper;
import com.aaami.order.repository.OrderRepository;
import com.aaami.order.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CreateOrderCommandHandler implements CommandHandler<CreateOrderCommand, OrderDto> {
    
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final DiscountService discountService;
    
    // TODO: Inject user service client to get user role
    // For now, we'll use a placeholder - in real implementation, fetch from user service
    private String getUserRole(Long userId) {
        // This should call user-service to get user role
        // For now, returning a default value
        return "USER";
    }
    
    @Override
    @Transactional
    public OrderDto handle(CreateOrderCommand command) {
        // TODO: Validate stock availability with product-service
        // TODO: Decrease inventory after order creation
        
        Order order = Order.builder()
                .userId(command.getUserId())
                .status(com.aaami.shared.dto.OrderStatus.PENDING)
                .items(new ArrayList<>())
                .build();
        
        BigDecimal orderSubtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (OrderItemCommand itemCommand : command.getItems()) {
            BigDecimal itemSubtotal = itemCommand.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemCommand.getQuantity()));
            
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productId(itemCommand.getProductId())
                    .quantity(itemCommand.getQuantity())
                    .unitPrice(itemCommand.getUnitPrice())
                    .totalPrice(itemSubtotal)
                    .discountApplied(BigDecimal.ZERO)
                    .build();
            
            orderItems.add(orderItem);
            orderSubtotal = orderSubtotal.add(itemSubtotal);
        }
        
        // Calculate discount
        String userRole = getUserRole(command.getUserId());
        BigDecimal discountRate = discountService.calculateDiscount(userRole, orderSubtotal);
        BigDecimal discountAmount = discountService.applyDiscount(orderSubtotal, discountRate);
        BigDecimal orderTotal = orderSubtotal.subtract(discountAmount);
        
        // Apply discount proportionally to items
        if (discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            for (OrderItem item : orderItems) {
                BigDecimal itemDiscount = item.getTotalPrice()
                        .multiply(discountRate)
                        .setScale(2, java.math.RoundingMode.HALF_UP);
                item.setDiscountApplied(itemDiscount);
                item.setTotalPrice(item.getTotalPrice().subtract(itemDiscount));
            }
        }
        
        order.setItems(orderItems);
        order.setOrderTotal(orderTotal);
        order.setStatus(com.aaami.shared.dto.OrderStatus.CONFIRMED);
        
        Order savedOrder = orderRepository.save(order);
        return orderMapper.toDto(savedOrder);
    }
}

