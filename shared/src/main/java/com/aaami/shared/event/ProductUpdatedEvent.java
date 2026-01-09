package com.aaami.shared.event;

import com.aaami.shared.dto.ProductDto;

/**
 * Event published when a product is updated.
 */
public class ProductUpdatedEvent extends ProductEvent {
    public ProductUpdatedEvent() {
        super();
    }

    public ProductUpdatedEvent(ProductDto product) {
        super(product);
    }

    public ProductUpdatedEvent(String eventId, java.time.LocalDateTime timestamp, ProductDto product) {
        super(eventId, timestamp, product);
    }
}

