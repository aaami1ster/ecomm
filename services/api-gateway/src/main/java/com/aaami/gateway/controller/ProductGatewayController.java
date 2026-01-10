package com.aaami.gateway.controller;

import com.aaami.gateway.client.ProductServiceClient;
import com.aaami.shared.command.CreateProductCommand;
import com.aaami.shared.command.UpdateProductCommand;
import com.aaami.shared.dto.PaginatedResponse;
import com.aaami.shared.dto.ProductDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.aaami.gateway.config.ApiVersion;

import java.math.BigDecimal;

@RestController
@RequestMapping({ApiVersion.V1 + "/products", ApiVersion.BASE + "/products"}) // Support both v1 and non-versioned for backward compatibility
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management endpoints (v1). GET operations are available to all authenticated users. CRUD operations require ADMIN role. Non-versioned paths are deprecated.")
public class ProductGatewayController {
    
    private final ProductServiceClient productServiceClient;
    
    @Operation(
            summary = "Create a new product",
            description = "Creates a new product. Requires ADMIN role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Product created successfully",
                    content = @Content(schema = @Schema(implementation = ProductDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or invalid JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - ADMIN role required",
                    content = @Content
            )
    })
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody CreateProductCommand command) {
        ProductDto product = productServiceClient.createProduct(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }
    
    @Operation(
            summary = "Get product by ID",
            description = "Retrieves a product by its ID. Available to all authenticated users (USER, PREMIUM_USER, ADMIN).",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Product found",
                    content = @Content(schema = @Schema(implementation = ProductDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or invalid JWT token",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProduct(
            @Parameter(description = "Product ID", example = "1", required = true)
            @PathVariable("id") Long id) {
        ProductDto product = productServiceClient.getProduct(id);
        return ResponseEntity.ok(product);
    }
    
    @Operation(
            summary = "Search products",
            description = "Searches and filters products with pagination. Available to all authenticated users (USER, PREMIUM_USER, ADMIN).",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Products retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PaginatedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or invalid JWT token",
                    content = @Content
            )
    })
    @GetMapping
    public ResponseEntity<PaginatedResponse<ProductDto>> searchProducts(
            @Parameter(description = "Filter by product name (partial match)", example = "laptop")
            @RequestParam(value = "name", required = false) String name,
            @Parameter(description = "Minimum price filter", example = "100.00")
            @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum price filter", example = "1000.00")
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
            @Parameter(description = "Filter only available products (quantity > 0)", example = "true")
            @RequestParam(value = "availableOnly", required = false) Boolean availableOnly,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size,
            @Parameter(description = "Sort field (e.g., name, price, createdAt)", example = "name")
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @Parameter(description = "Sort direction (asc or desc)", example = "asc")
            @RequestParam(value = "sortDirection", required = false, defaultValue = "asc") String sortDirection) {
        PaginatedResponse<ProductDto> response = productServiceClient.searchProducts(name, minPrice, maxPrice, availableOnly, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "Update a product",
            description = "Updates an existing product. Requires ADMIN role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Product updated successfully",
                    content = @Content(schema = @Schema(implementation = ProductDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or invalid JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - ADMIN role required",
                    content = @Content
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(
            @Parameter(description = "Product ID", example = "1", required = true)
            @PathVariable("id") Long id,
            @RequestBody UpdateProductCommand command) {
        command.setId(id);
        ProductDto product = productServiceClient.updateProduct(id, command);
        return ResponseEntity.ok(product);
    }
    
    @Operation(
            summary = "Delete a product",
            description = "Deletes a product by ID. Requires ADMIN role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Product deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or invalid JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - ADMIN role required",
                    content = @Content
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID", example = "1", required = true)
            @PathVariable("id") Long id) {
        productServiceClient.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}

