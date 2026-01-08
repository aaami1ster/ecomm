package com.aaami.product.controller;

import com.aaami.cqrs.CommandBus;
import com.aaami.cqrs.QueryBus;
import com.aaami.shared.command.CreateProductCommand;
import com.aaami.shared.command.DeleteProductCommand;
import com.aaami.shared.command.UpdateProductCommand;
import com.aaami.shared.dto.PaginatedResponse;
import com.aaami.shared.dto.ProductDto;
import com.aaami.product.query.GetProductQuery;
import com.aaami.product.query.SearchProductsQuery;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final CommandBus commandBus;
    private final QueryBus queryBus;
    
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody CreateProductCommand command) {
        ProductDto product = commandBus.dispatch(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable("id") Long id) {
        GetProductQuery query = new GetProductQuery(id);
        ProductDto product = queryBus.dispatch(query);
        return ResponseEntity.ok(product);
    }
    
    @GetMapping
    public ResponseEntity<PaginatedResponse<ProductDto>> searchProducts(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "minPrice", required = false) java.math.BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false) java.math.BigDecimal maxPrice,
            @RequestParam(value = "availableOnly", required = false) Boolean availableOnly,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "sortDirection", required = false, defaultValue = "asc") String sortDirection) {
        SearchProductsQuery query = SearchProductsQuery.builder()
                .name(name)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .availableOnly(availableOnly)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
        PaginatedResponse<ProductDto> response = queryBus.dispatch(query);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable("id") Long id,
            @RequestBody UpdateProductCommand command) {
        command.setId(id);
        ProductDto product = commandBus.dispatch(command);
        return ResponseEntity.ok(product);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") Long id) {
        DeleteProductCommand command = new DeleteProductCommand(id);
        commandBus.dispatch(command);
        return ResponseEntity.noContent().build();
    }
}

