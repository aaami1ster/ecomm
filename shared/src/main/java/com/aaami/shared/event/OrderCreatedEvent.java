package com.aaami.shared.event;

import com.aaami.shared.dto.OrderDto;

/**
 * Event published when a new order is created.
 */
public class OrderCreatedEvent extends OrderEvent {
    public OrderCreatedEvent() {
        super();
    }

    public OrderCreatedEvent(OrderDto order) {
        super(order);
    }

    public OrderCreatedEvent(String eventId, java.time.LocalDateTime timestamp, OrderDto order) {
        super(eventId, timestamp, order);
    }
}

