package com.aaami.product.handler;

import com.aaami.product.domain.Product;
import com.aaami.product.mapper.ProductMapper;
import com.aaami.product.query.SearchProductsQuery;
import com.aaami.product.repository.ProductRepository;
import com.aaami.shared.dto.PaginatedResponse;
import com.aaami.shared.dto.ProductDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchProductsQueryHandler Tests")
class SearchProductsQueryHandlerTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private SearchProductsQueryHandler handler;

    private SearchProductsQuery query;
    private Product product1;
    private Product product2;
    private ProductDto productDto1;
    private ProductDto productDto2;

    @BeforeEach
    void setUp() {
        query = new SearchProductsQuery();

        product1 = Product.builder()
                .id(1L)
                .name("Laptop")
                .description("High-performance laptop")
                .price(new BigDecimal("999.99"))
                .quantity(10)
                .build();

        product2 = Product.builder()
                .id(2L)
                .name("Mouse")
                .description("Wireless mouse")
                .price(new BigDecimal("29.99"))
                .quantity(50)
                .build();

        productDto1 = ProductDto.builder()
                .id(1L)
                .name("Laptop")
                .description("High-performance laptop")
                .price(new BigDecimal("999.99"))
                .quantity(10)
                .build();

        productDto2 = ProductDto.builder()
                .id(2L)
                .name("Mouse")
                .description("Wireless mouse")
                .price(new BigDecimal("29.99"))
                .quantity(50)
                .build();
    }

    @Test
    @DisplayName("Should return paginated products with default pagination")
    void handle_ShouldReturnPaginatedProducts_WithDefaultPagination() {
        // Given
        Page<Product> productPage = new PageImpl<>(List.of(product1, product2), PageRequest.of(0, 20), 2);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(productPage);
        when(productMapper.toDto(product1)).thenReturn(productDto1);
        when(productMapper.toDto(product2)).thenReturn(productDto2);

        // When
        PaginatedResponse<ProductDto> result = handler.handle(query);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(0, result.getPage());
        assertEquals(20, result.getSize());
        verify(productRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Should filter by name when provided")
    void handle_ShouldFilterByName_WhenProvided() {
        // Given
        query.setName("Laptop");
        Page<Product> productPage = new PageImpl<>(List.of(product1), PageRequest.of(0, 20), 1);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(productPage);
        when(productMapper.toDto(product1)).thenReturn(productDto1);

        // When
        PaginatedResponse<ProductDto> result = handler.handle(query);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Laptop", result.getContent().get(0).getName());
        verify(productRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Should filter by price range when provided")
    void handle_ShouldFilterByPriceRange_WhenProvided() {
        // Given
        query.setMinPrice(new BigDecimal("50.00"));
        query.setMaxPrice(new BigDecimal("1000.00"));
        Page<Product> productPage = new PageImpl<>(List.of(product1), PageRequest.of(0, 20), 1);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(productPage);
        when(productMapper.toDto(product1)).thenReturn(productDto1);

        // When
        PaginatedResponse<ProductDto> result = handler.handle(query);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(productRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Should filter available products only when availableOnly is true")
    void handle_ShouldFilterAvailableProducts_WhenAvailableOnlyIsTrue() {
        // Given
        query.setAvailableOnly(true);
        Page<Product> productPage = new PageImpl<>(List.of(product1, product2), PageRequest.of(0, 20), 2);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(productPage);
        when(productMapper.toDto(product1)).thenReturn(productDto1);
        when(productMapper.toDto(product2)).thenReturn(productDto2);

        // When
        PaginatedResponse<ProductDto> result = handler.handle(query);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(productRepository).findAll(any(Specification.class), any(Pageable.class));
    }
}

