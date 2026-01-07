package com.aaami.product.command;

import com.aaami.cqrs.Command;
import com.aaami.product.dto.ProductDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductCommand implements Command<ProductDto> {
    @NotNull(message = "Product ID is required")
    private Long id;
    
    private String name;
    
    private String description;
    
    @Positive(message = "Price must be positive")
    private BigDecimal price;
    
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
}

