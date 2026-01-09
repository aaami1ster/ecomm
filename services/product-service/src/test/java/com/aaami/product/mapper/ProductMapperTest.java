package com.aaami.product.mapper;

import com.aaami.product.domain.Product;
import com.aaami.shared.dto.ProductDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProductMapper Tests")
class ProductMapperTest {

    private ProductMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProductMapper();
    }

    @Test
    @DisplayName("Should map Product to ProductDto")
    void toDto_ShouldMapProductToDto() {
        // Given
        Product product = Product.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .quantity(10)
                .build();

        // When
        ProductDto dto = mapper.toDto(product);

        // Then
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Test Product", dto.getName());
        assertEquals("Test Description", dto.getDescription());
        assertEquals(new BigDecimal("99.99"), dto.getPrice());
        assertEquals(10, dto.getQuantity());
    }

    @Test
    @DisplayName("Should return null when product is null")
    void toDto_ShouldReturnNull_WhenProductIsNull() {
        // When
        ProductDto dto = mapper.toDto(null);

        // Then
        assertNull(dto);
    }

    @Test
    @DisplayName("Should map ProductDto to Product")
    void toEntity_ShouldMapDtoToProduct() {
        // Given
        ProductDto dto = ProductDto.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .quantity(10)
                .build();

        // When
        Product product = mapper.toEntity(dto);

        // Then
        assertNotNull(product);
        assertEquals(1L, product.getId());
        assertEquals("Test Product", product.getName());
        assertEquals("Test Description", product.getDescription());
        assertEquals(new BigDecimal("99.99"), product.getPrice());
        assertEquals(10, product.getQuantity());
    }

    @Test
    @DisplayName("Should return null when dto is null")
    void toEntity_ShouldReturnNull_WhenDtoIsNull() {
        // When
        Product product = mapper.toEntity(null);

        // Then
        assertNull(product);
    }
}

