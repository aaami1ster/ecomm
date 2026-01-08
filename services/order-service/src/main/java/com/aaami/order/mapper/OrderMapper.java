package com.aaami.order.mapper;

import com.aaami.order.domain.Order;
import com.aaami.order.domain.OrderItem;
import com.aaami.shared.dto.OrderDto;
import com.aaami.shared.dto.OrderItemDto;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class OrderMapper {
    
    public OrderDto toDto(Order order) {
        if (order == null) {
            return null;
        }
        return OrderDto.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .items(order.getItems().stream()
                        .map(this::toItemDto)
                        .collect(Collectors.toList()))
                .orderTotal(order.getOrderTotal())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
    
    public OrderItemDto toItemDto(OrderItem item) {
        if (item == null) {
            return null;
        }
        return OrderItemDto.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .discountApplied(item.getDiscountApplied())
                .totalPrice(item.getTotalPrice())
                .build();
    }
}

