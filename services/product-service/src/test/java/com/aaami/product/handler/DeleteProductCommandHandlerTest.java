package com.aaami.product.handler;

import com.aaami.product.domain.Product;
import com.aaami.product.repository.ProductRepository;
import com.aaami.shared.command.DeleteProductCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteProductCommandHandler Tests")
class DeleteProductCommandHandlerTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private DeleteProductCommandHandler handler;

    private DeleteProductCommand command;
    private Product product;

    @BeforeEach
    void setUp() {
        command = new DeleteProductCommand();
        command.setId(1L);

        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .quantity(10)
                .build();
    }

    @Test
    @DisplayName("Should soft delete product when product exists")
    void handle_ShouldSoftDeleteProduct_WhenProductExists() {
        // Given
        when(productRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        handler.handle(command);

        // Then
        assertNotNull(product.getDeletedAt());
        verify(productRepository).findByIdAndDeletedAtIsNull(1L);
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when product does not exist")
    void handle_ShouldThrowException_WhenProductNotFound() {
        // Given
        when(productRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
        assertEquals("Product not found with id: 1", exception.getMessage());
        verify(productRepository).findByIdAndDeletedAtIsNull(1L);
        verify(productRepository, never()).save(any(Product.class));
    }
}

