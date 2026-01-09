package com.aaami.shared.event;

import com.aaami.shared.dto.OrderDto;
import com.aaami.shared.dto.OrderStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Event published when an order status changes.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class OrderStatusChangedEvent extends OrderEvent {
    private OrderStatus previousStatus;
    private OrderStatus newStatus;

    public OrderStatusChangedEvent(OrderDto order, OrderStatus previousStatus, OrderStatus newStatus) {
        super(order);
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
    }

    public OrderStatusChangedEvent(String eventId, java.time.LocalDateTime timestamp, 
                                   OrderDto order, OrderStatus previousStatus, OrderStatus newStatus) {
        super(eventId, timestamp, order);
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
    }
}

