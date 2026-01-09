package com.aaami.shared.event;

import com.aaami.shared.dto.ProductDto;

/**
 * Event published when a new product is created.
 */
public class ProductCreatedEvent extends ProductEvent {
    public ProductCreatedEvent() {
        super();
    }

    public ProductCreatedEvent(ProductDto product) {
        super(product);
    }

    public ProductCreatedEvent(String eventId, java.time.LocalDateTime timestamp, ProductDto product) {
        super(eventId, timestamp, product);
    }
}

