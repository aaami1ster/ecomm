package com.aaami.shared.event;

import com.aaami.shared.dto.ProductDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Event published when product inventory is decreased.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class InventoryDecreasedEvent extends ProductEvent {
    private Integer quantityDecreased;
    private Integer remainingQuantity;

    public InventoryDecreasedEvent(ProductDto product, Integer quantityDecreased, Integer remainingQuantity) {
        super(product);
        this.quantityDecreased = quantityDecreased;
        this.remainingQuantity = remainingQuantity;
    }

    public InventoryDecreasedEvent(String eventId, java.time.LocalDateTime timestamp, 
                                   ProductDto product, Integer quantityDecreased, Integer remainingQuantity) {
        super(eventId, timestamp, product);
        this.quantityDecreased = quantityDecreased;
        this.remainingQuantity = remainingQuantity;
    }
}

