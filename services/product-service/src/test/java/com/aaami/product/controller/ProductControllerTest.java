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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController Tests")
class ProductControllerTest {

    @Mock
    private CommandBus commandBus;

    @Mock
    private QueryBus queryBus;

    @InjectMocks
    private ProductController controller;

    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        productDto = ProductDto.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .quantity(10)
                .build();
    }

    @Test
    @DisplayName("Should create product successfully")
    void createProduct_ShouldReturnCreated_WhenProductCreated() {
        // Given
        CreateProductCommand command = new CreateProductCommand();
        command.setName("Test Product");
        when(commandBus.dispatch(any(CreateProductCommand.class))).thenReturn(productDto);

        // When
        ResponseEntity<ProductDto> response = controller.createProduct(command);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Product", response.getBody().getName());
        verify(commandBus).dispatch(any(CreateProductCommand.class));
    }

    @Test
    @DisplayName("Should get product by ID")
    void getProduct_ShouldReturnProduct_WhenProductExists() {
        // Given
        when(queryBus.dispatch(any(GetProductQuery.class))).thenReturn(productDto);

        // When
        ResponseEntity<ProductDto> response = controller.getProduct(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(queryBus).dispatch(any(GetProductQuery.class));
    }

    @Test
    @DisplayName("Should search products with pagination")
    void searchProducts_ShouldReturnPaginatedResponse() {
        // Given
        PaginatedResponse<ProductDto> paginatedResponse = PaginatedResponse.<ProductDto>builder()
                .content(java.util.List.of(productDto))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();
        when(queryBus.dispatch(any(SearchProductsQuery.class))).thenReturn(paginatedResponse);

        // When
        ResponseEntity<PaginatedResponse<ProductDto>> response = controller.searchProducts(
                null, null, null, null, 0, 20, null, "asc");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        verify(queryBus).dispatch(any(SearchProductsQuery.class));
    }

    @Test
    @DisplayName("Should update product successfully")
    void updateProduct_ShouldReturnUpdatedProduct() {
        // Given
        UpdateProductCommand command = new UpdateProductCommand();
        command.setName("Updated Product");
        when(commandBus.dispatch(any(UpdateProductCommand.class))).thenReturn(productDto);

        // When
        ResponseEntity<ProductDto> response = controller.updateProduct(1L, command);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, command.getId());
        verify(commandBus).dispatch(any(UpdateProductCommand.class));
    }

    @Test
    @DisplayName("Should delete product successfully")
    void deleteProduct_ShouldReturnNoContent() {
        // Given
        when(commandBus.dispatch(any(DeleteProductCommand.class))).thenReturn(null);

        // When
        ResponseEntity<Void> response = controller.deleteProduct(1L);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(commandBus).dispatch(any(DeleteProductCommand.class));
    }
}

