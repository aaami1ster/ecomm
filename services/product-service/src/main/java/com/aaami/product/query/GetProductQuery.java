package com.aaami.product.query;

import com.aaami.cqrs.Query;
import com.aaami.product.dto.ProductDto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetProductQuery implements Query<ProductDto> {
    @NotNull(message = "Product ID is required")
    private Long id;
}

