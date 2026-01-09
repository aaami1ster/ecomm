package com.aaami.shared.event;

import com.aaami.shared.dto.ProductDto;

/**
 * Event published when a product is deleted (soft delete).
 */
public class ProductDeletedEvent extends ProductEvent {
    public ProductDeletedEvent() {
        super();
    }

    public ProductDeletedEvent(ProductDto product) {
        super(product);
    }

    public ProductDeletedEvent(String eventId, java.time.LocalDateTime timestamp, ProductDto product) {
        super(eventId, timestamp, product);
    }
}

