package com.aaami.product.query;

import com.aaami.cqrs.Query;
import com.aaami.shared.dto.PaginatedResponse;
import com.aaami.shared.dto.ProductDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchProductsQuery implements Query<PaginatedResponse<ProductDto>> {
    private String name;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean availableOnly;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection; // "asc" or "desc"
}

