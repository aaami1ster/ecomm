package com.aaami.shared.command;

import com.aaami.cqrs.Command;
import com.aaami.shared.dto.ProductDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DecreaseProductQuantityCommand implements Command<ProductDto> {
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @NotNull(message = "Quantity to decrease is required")
    @Positive(message = "Quantity to decrease must be positive")
    private Integer quantity;
}

