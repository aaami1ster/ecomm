package com.aaami.order.handler;

import com.aaami.cqrs.CommandHandler;
import com.aaami.shared.command.CreateOrderCommand;
import com.aaami.shared.command.OrderItemCommand;
import com.aaami.order.client.ProductServiceClient;
import com.aaami.order.domain.Order;
import com.aaami.order.domain.OrderItem;
import com.aaami.shared.dto.OrderDto;
import com.aaami.shared.dto.ProductDto;
import com.aaami.order.mapper.OrderMapper;
import com.aaami.order.repository.OrderRepository;
import com.aaami.order.service.DiscountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateOrderCommandHandler implements CommandHandler<CreateOrderCommand, OrderDto> {
    
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final DiscountService discountService;
    private final ProductServiceClient productServiceClient;
    
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
        Order order = Order.builder()
                .userId(command.getUserId())
                .status(com.aaami.shared.dto.OrderStatus.PENDING)
                .items(new ArrayList<>())
                .build();
        
        BigDecimal orderSubtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        
        // Fetch product details and calculate prices for each item
        for (OrderItemCommand itemCommand : command.getItems()) {
            // Fetch product details from product service
            ProductDto product;
            try {
                product = productServiceClient.getProduct(itemCommand.getProductId());
            } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
                throw new IllegalArgumentException("Product not found with id: " + itemCommand.getProductId());
            } catch (org.springframework.web.client.RestClientException e) {
                log.error("Error fetching product {} from product service: {}", itemCommand.getProductId(), e.getMessage());
                throw new IllegalStateException("Unable to fetch product details. Please try again later.");
            }
            
            if (product == null) {
                throw new IllegalArgumentException("Product not found with id: " + itemCommand.getProductId());
            }
            
            // Validate stock availability
            if (product.getQuantity() < itemCommand.getQuantity()) {
                throw new IllegalArgumentException(
                    String.format("Insufficient stock for product %d. Available: %d, Requested: %d",
                        product.getId(), product.getQuantity(), itemCommand.getQuantity()));
            }
            
            // Get unit price from product
            BigDecimal unitPrice = product.getPrice();
            
            // Calculate item subtotal (before discount)
            BigDecimal itemSubtotal = unitPrice.multiply(BigDecimal.valueOf(itemCommand.getQuantity()));
            
            // Create order item with unit price from product
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productId(itemCommand.getProductId())
                    .quantity(itemCommand.getQuantity())
                    .unitPrice(unitPrice)
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
        log.info("Order created successfully with id: {} for user: {}", savedOrder.getId(), command.getUserId());
        return orderMapper.toDto(savedOrder);
    }
}

