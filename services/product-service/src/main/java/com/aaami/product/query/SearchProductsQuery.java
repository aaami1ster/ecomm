package com.aaami.product.query;

import com.aaami.cqrs.Query;
import com.aaami.shared.dto.ProductDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchProductsQuery implements Query<List<ProductDto>> {
    private String name;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean availableOnly;
}

