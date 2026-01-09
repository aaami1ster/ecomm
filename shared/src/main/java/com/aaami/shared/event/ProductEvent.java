package com.aaami.shared.event;

import com.aaami.shared.dto.ProductDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Base class for product-related events.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public abstract class ProductEvent extends BaseEvent {
    private ProductDto product;

    protected ProductEvent(ProductDto product) {
        super();
        this.product = product;
    }

    protected ProductEvent(String eventId, java.time.LocalDateTime timestamp, ProductDto product) {
        super(eventId, timestamp);
        this.product = product;
    }
}

