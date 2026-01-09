package com.aaami.gateway.controller;

import com.aaami.gateway.client.ProductServiceClient;
import com.aaami.shared.command.CreateProductCommand;
import com.aaami.shared.command.UpdateProductCommand;
import com.aaami.shared.dto.PaginatedResponse;
import com.aaami.shared.dto.ProductDto;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductGatewayController Tests")
class ProductGatewayControllerTest {

    @Mock
    private ProductServiceClient productServiceClient;

    @InjectMocks
    private ProductGatewayController controller;

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
    void createProduct_ShouldReturnCreated() {
        // Given
        CreateProductCommand command = new CreateProductCommand();
        when(productServiceClient.createProduct(any(CreateProductCommand.class))).thenReturn(productDto);

        // When
        ResponseEntity<ProductDto> response = controller.createProduct(command);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(productServiceClient).createProduct(any(CreateProductCommand.class));
    }

    @Test
    @DisplayName("Should get product by ID")
    void getProduct_ShouldReturnProduct() {
        // Given
        when(productServiceClient.getProduct(1L)).thenReturn(productDto);

        // When
        ResponseEntity<ProductDto> response = controller.getProduct(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(productServiceClient).getProduct(1L);
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
        when(productServiceClient.searchProducts(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(paginatedResponse);

        // When
        ResponseEntity<PaginatedResponse<ProductDto>> response = controller.searchProducts(
                null, null, null, null, 0, 20, null, "asc");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        verify(productServiceClient).searchProducts(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should update product successfully")
    void updateProduct_ShouldReturnUpdatedProduct() {
        // Given
        UpdateProductCommand command = new UpdateProductCommand();
        when(productServiceClient.updateProduct(anyLong(), any(UpdateProductCommand.class))).thenReturn(productDto);

        // When
        ResponseEntity<ProductDto> response = controller.updateProduct(1L, command);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productServiceClient).updateProduct(1L, command);
    }

    @Test
    @DisplayName("Should delete product successfully")
    void deleteProduct_ShouldReturnNoContent() {
        // Given
        doNothing().when(productServiceClient).deleteProduct(1L);

        // When
        ResponseEntity<Void> response = controller.deleteProduct(1L);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(productServiceClient).deleteProduct(1L);
    }
}

