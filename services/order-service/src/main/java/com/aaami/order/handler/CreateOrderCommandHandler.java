package com.aaami.order.handler;

import com.aaami.cqrs.CommandHandler;
import com.aaami.shared.command.CreateOrderCommand;
import com.aaami.shared.command.OrderItemCommand;
import com.aaami.order.client.ProductServiceClient;
import com.aaami.order.client.UserServiceClient;
import com.aaami.order.domain.Order;
import com.aaami.order.domain.OrderItem;
import com.aaami.shared.dto.OrderDto;
import com.aaami.shared.dto.ProductDto;
import com.aaami.shared.dto.UserDto;
import com.aaami.order.mapper.OrderMapper;
import com.aaami.order.repository.OrderRepository;
import com.aaami.order.service.IdempotencyService;
import com.aaami.order.service.OrderEventProducer;
import com.aaami.discount.DiscountService;
import com.aaami.shared.dto.UserRole;
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
    private final UserServiceClient userServiceClient;
    private final IdempotencyService idempotencyService;
    private final OrderEventProducer eventProducer;
    
    private UserRole getUserRole(Long userId) {
        try {
            UserDto user = userServiceClient.getUser(userId);
            if (user == null) {
                log.error("User {} not found in user service", userId);
                throw new IllegalArgumentException("User not found with id: " + userId);
            }
            if (user.getRole() == null) {
                log.error("User {} has no role assigned", userId);
                throw new IllegalStateException("User " + userId + " has no role assigned");
            }
            return user.getRole();
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            log.error("User {} not found in user service", userId);
            throw new IllegalArgumentException("User not found with id: " + userId);
        } catch (org.springframework.web.client.RestClientException e) {
            log.error("Error fetching user {} from user service: {}", userId, e.getMessage());
            throw new IllegalStateException("Unable to fetch user details. Please try again later.", e);
        }
    }
    
    @Override
    @Transactional
    public OrderDto handle(CreateOrderCommand command) {
        // Check idempotency key first
        if (command.getIdempotencyKey() != null && !command.getIdempotencyKey().isEmpty()) {
            OrderDto cachedOrder = idempotencyService.getCachedOrder(command.getIdempotencyKey());
            if (cachedOrder != null) {
                log.info("Returning cached order for idempotency key: {}", command.getIdempotencyKey());
                return cachedOrder;
            }
        }
        
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
        
        // Get user role from user service
        UserRole userRole = getUserRole(command.getUserId());
        
        // Calculate discount
        BigDecimal discountAmount = discountService.calculateDiscount(orderSubtotal, userRole);
        BigDecimal orderTotal = orderSubtotal.subtract(discountAmount);
        
        // Apply discount proportionally to items
        if (discountAmount.compareTo(BigDecimal.ZERO) > 0 && orderSubtotal.compareTo(BigDecimal.ZERO) > 0) {
            // Calculate discount rate as a percentage of the order subtotal
            BigDecimal discountRate = discountAmount.divide(orderSubtotal, 4, java.math.RoundingMode.HALF_UP);
            
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
        // Update the order status to CONFIRMED
        order.setStatus(com.aaami.shared.dto.OrderStatus.CONFIRMED);
        
        // Decrease product inventory for each item in the order BEFORE saving the order
        // This ensures that if inventory decrease fails, the order won't be created
        try {
            for (OrderItem orderItem : orderItems) {
                productServiceClient.decreaseProductQuantity(orderItem.getProductId(), orderItem.getQuantity());
                log.debug("Decreased quantity for product {} by {}", orderItem.getProductId(), orderItem.getQuantity());
            }
            log.info("Successfully decreased inventory for all products before creating order");
        } catch (Exception e) {
            log.error("Failed to decrease inventory: {}", e.getMessage());
            throw new IllegalStateException("Failed to update inventory: " + e.getMessage(), e);
        }
        
        // Save the order after successfully decreasing inventory
        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with id: {} for user: {} with status: CONFIRMED", savedOrder.getId(), command.getUserId());
        
        OrderDto orderDto = orderMapper.toDto(savedOrder);
        
        // Cache the order with idempotency key if provided
        if (command.getIdempotencyKey() != null && !command.getIdempotencyKey().isEmpty()) {
            idempotencyService.cacheOrder(command.getIdempotencyKey(), orderDto);
        }
        
        // Publish event after successful order creation
        eventProducer.publishOrderCreated(orderDto);
        
        return orderDto;
    }
}

