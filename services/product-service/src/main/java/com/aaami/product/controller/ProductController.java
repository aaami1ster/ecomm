package com.aaami.product.controller;

import com.aaami.cqrs.CommandBus;
import com.aaami.cqrs.QueryBus;
import com.aaami.product.command.CreateProductCommand;
import com.aaami.product.command.DeleteProductCommand;
import com.aaami.product.command.UpdateProductCommand;
import com.aaami.product.dto.ProductDto;
import com.aaami.product.query.GetProductQuery;
import com.aaami.product.query.SearchProductsQuery;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<ProductDto> getProduct(@PathVariable Long id) {
        GetProductQuery query = new GetProductQuery(id);
        ProductDto product = queryBus.dispatch(query);
        return ResponseEntity.ok(product);
    }
    
    @GetMapping
    public ResponseEntity<List<ProductDto>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(required = false) Boolean availableOnly) {
        SearchProductsQuery query = new SearchProductsQuery(name, minPrice, maxPrice, availableOnly);
        List<ProductDto> products = queryBus.dispatch(query);
        return ResponseEntity.ok(products);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductCommand command) {
        command.setId(id);
        ProductDto product = commandBus.dispatch(command);
        return ResponseEntity.ok(product);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        DeleteProductCommand command = new DeleteProductCommand(id);
        commandBus.dispatch(command);
        return ResponseEntity.noContent().build();
    }
}

