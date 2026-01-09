package com.aaami.product.handler;

import com.aaami.shared.command.DecreaseProductQuantityCommand;
import com.aaami.shared.dto.ProductDto;
import com.aaami.product.domain.Product;
import com.aaami.product.mapper.ProductMapper;
import com.aaami.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DecreaseProductQuantityCommandHandlerTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private DecreaseProductQuantityCommandHandler handler;

    private DecreaseProductQuantityCommand command;
    private Product product;
    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        command = new DecreaseProductQuantityCommand();
        command.setProductId(1L);
        command.setQuantity(10);

        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .quantity(100)
                .build();

        productDto = ProductDto.builder()
                .id(1L)
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .quantity(90)
                .build();
    }

    @Test
    void handle_ShouldDecreaseQuantity_WhenStockIsSufficient() {
        // Given
        when(productRepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toDto(any(Product.class))).thenReturn(productDto);

        // When
        ProductDto result = handler.handle(command);

        // Then
        assertNotNull(result);
        assertEquals(90, product.getQuantity());
        verify(productRepository).findByIdAndDeletedAtIsNull(command.getProductId());
        verify(productRepository).save(product);
    }

    @Test
    void handle_ShouldThrowException_WhenProductNotFound() {
        // Given
        when(productRepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> handler.handle(command));
        assertTrue(exception.getMessage().contains("Product not found"));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void handle_ShouldThrowException_WhenInsufficientStock() {
        // Given
        command.setQuantity(150); // More than available
        when(productRepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.of(product));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> handler.handle(command));
        assertTrue(exception.getMessage().contains("Insufficient stock"));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void handle_ShouldDecreaseToZero_WhenQuantityEqualsStock() {
        // Given
        command.setQuantity(100); // Exactly available
        when(productRepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toDto(any(Product.class))).thenReturn(productDto);

        // When
        handler.handle(command);

        // Then
        assertEquals(0, product.getQuantity());
        verify(productRepository).save(product);
    }
}

