package com.aaami.shared.event;

import com.aaami.shared.dto.OrderDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Base class for order-related events.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public abstract class OrderEvent extends BaseEvent {
    private OrderDto order;

    protected OrderEvent(OrderDto order) {
        super();
        this.order = order;
    }

    protected OrderEvent(String eventId, java.time.LocalDateTime timestamp, OrderDto order) {
        super(eventId, timestamp);
        this.order = order;
    }
}

