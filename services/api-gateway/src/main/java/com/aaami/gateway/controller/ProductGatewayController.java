package com.aaami.gateway.controller;

import com.aaami.gateway.client.ProductServiceClient;
import com.aaami.shared.command.CreateProductCommand;
import com.aaami.shared.command.UpdateProductCommand;
import com.aaami.shared.dto.ProductDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductGatewayController {
    
    private final ProductServiceClient productServiceClient;
    
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody CreateProductCommand command) {
        ProductDto product = productServiceClient.createProduct(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable("id") Long id) {
        ProductDto product = productServiceClient.getProduct(id);
        return ResponseEntity.ok(product);
    }
    
    @GetMapping
    public ResponseEntity<List<ProductDto>> searchProducts(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(value = "availableOnly", required = false) Boolean availableOnly) {
        List<ProductDto> products = productServiceClient.searchProducts(name, minPrice, maxPrice, availableOnly);
        return ResponseEntity.ok(products);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable("id") Long id,
            @RequestBody UpdateProductCommand command) {
        command.setId(id);
        ProductDto product = productServiceClient.updateProduct(id, command);
        return ResponseEntity.ok(product);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") Long id) {
        productServiceClient.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}

