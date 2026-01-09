package com.aaami.product.handler;

import com.aaami.product.domain.Product;
import com.aaami.product.mapper.ProductMapper;
import com.aaami.product.query.GetProductQuery;
import com.aaami.product.repository.ProductRepository;
import com.aaami.product.service.ProductCacheService;
import com.aaami.shared.dto.ProductDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetProductQueryHandler Tests")
class GetProductQueryHandlerTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductCacheService productCacheService;

    @InjectMocks
    private GetProductQueryHandler handler;

    private GetProductQuery query;
    private Product product;
    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        query = new GetProductQuery();
        query.setId(1L);

        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .quantity(10)
                .build();

        productDto = ProductDto.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .quantity(10)
                .build();
    }

    @Test
    @DisplayName("Should return product DTO when product exists")
    void handle_ShouldReturnProductDto_WhenProductExists() {
        // Given - cache miss scenario
        when(productCacheService.getCachedProduct(1L)).thenReturn(null);
        when(productRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(product));
        when(productMapper.toDto(product)).thenReturn(productDto);
        doNothing().when(productCacheService).cacheProduct(any(ProductDto.class));

        // When
        ProductDto result = handler.handle(query);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Product", result.getName());
        verify(productCacheService).getCachedProduct(1L);
        verify(productRepository).findByIdAndDeletedAtIsNull(1L);
        verify(productMapper).toDto(product);
        verify(productCacheService).cacheProduct(productDto);
    }

    @Test
    @DisplayName("Should return cached product when available")
    void handle_ShouldReturnCachedProduct_WhenCacheHit() {
        // Given - cache hit scenario
        when(productCacheService.getCachedProduct(1L)).thenReturn(productDto);

        // When
        ProductDto result = handler.handle(query);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Product", result.getName());
        verify(productCacheService).getCachedProduct(1L);
        verify(productRepository, never()).findByIdAndDeletedAtIsNull(anyLong());
        verify(productMapper, never()).toDto(any());
        verify(productCacheService, never()).cacheProduct(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when product does not exist")
    void handle_ShouldThrowException_WhenProductNotFound() {
        // Given
        when(productRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> handler.handle(query));
        assertEquals("Product not found with id: 1", exception.getMessage());
        verify(productRepository).findByIdAndDeletedAtIsNull(1L);
        verify(productMapper, never()).toDto(any());
    }
}

