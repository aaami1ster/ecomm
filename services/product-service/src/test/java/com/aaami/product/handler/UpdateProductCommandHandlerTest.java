package com.aaami.product.handler;

import com.aaami.product.domain.Product;
import com.aaami.product.mapper.ProductMapper;
import com.aaami.product.repository.ProductRepository;
import com.aaami.product.service.ProductCacheService;
import com.aaami.product.service.ProductEventProducer;
import com.aaami.product.exception.DuplicateProductNameException;
import com.aaami.shared.command.UpdateProductCommand;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateProductCommandHandler Tests")
class UpdateProductCommandHandlerTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductCacheService productCacheService;

    @Mock
    private ProductEventProducer eventProducer;

    @InjectMocks
    private UpdateProductCommandHandler handler;

    private UpdateProductCommand command;
    private Product product;
    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        command = new UpdateProductCommand();
        command.setId(1L);

        product = Product.builder()
                .id(1L)
                .name("Original Name")
                .description("Original Description")
                .price(new BigDecimal("99.99"))
                .quantity(10)
                .build();

        productDto = ProductDto.builder()
                .id(1L)
                .name("Updated Name")
                .description("Updated Description")
                .price(new BigDecimal("149.99"))
                .quantity(20)
                .build();
    }

    @Test
    @DisplayName("Should update product when all fields are provided")
    void handle_ShouldUpdateProduct_WhenAllFieldsProvided() {
        // Given
        command.setName("Updated Name");
        command.setDescription("Updated Description");
        command.setPrice(new BigDecimal("149.99"));
        command.setQuantity(20);

        when(productRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(product));
        when(productRepository.existsByNameAndDeletedAtIsNull(anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(productDto);
        doNothing().when(productCacheService).invalidateProduct(anyLong());
        doNothing().when(productCacheService).cacheProduct(any(ProductDto.class));
        doNothing().when(eventProducer).publishProductUpdated(any(ProductDto.class));

        // When
        ProductDto result = handler.handle(command);

        // Then
        assertNotNull(result);
        assertEquals("Updated Name", product.getName());
        assertEquals("Updated Description", product.getDescription());
        assertEquals(new BigDecimal("149.99"), product.getPrice());
        assertEquals(20, product.getQuantity());
        verify(productRepository).findByIdAndDeletedAtIsNull(1L);
        verify(productRepository).existsByNameAndDeletedAtIsNull("Updated Name");
        verify(productRepository).save(product);
        verify(productMapper).toDto(product);
        verify(eventProducer).publishProductUpdated(any(ProductDto.class));
    }

    @Test
    @DisplayName("Should update only provided fields")
    void handle_ShouldUpdateOnlyProvidedFields() {
        // Given
        command.setName("Updated Name");
        // Other fields are null

        when(productRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(product));
        when(productRepository.existsByNameAndDeletedAtIsNull(anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(productDto);
        doNothing().when(productCacheService).invalidateProduct(anyLong());
        doNothing().when(productCacheService).cacheProduct(any(ProductDto.class));
        doNothing().when(eventProducer).publishProductUpdated(any(ProductDto.class));

        // When
        handler.handle(command);

        // Then
        assertEquals("Updated Name", product.getName());
        assertEquals("Original Description", product.getDescription()); // Unchanged
        verify(productRepository).existsByNameAndDeletedAtIsNull("Updated Name");
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

    @Test
    @DisplayName("Should throw DuplicateProductNameException when updating to an existing name")
    void handle_ShouldThrowException_WhenUpdatingToExistingName() {
        // Given
        command.setName("Existing Product Name");
        when(productRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(product));
        when(productRepository.existsByNameAndDeletedAtIsNull("Existing Product Name")).thenReturn(true);

        // When & Then
        DuplicateProductNameException exception = assertThrows(
                DuplicateProductNameException.class,
                () -> handler.handle(command)
        );
        
        assertEquals("Product with name 'Existing Product Name' already exists", exception.getMessage());
        verify(productRepository).findByIdAndDeletedAtIsNull(1L);
        verify(productRepository).existsByNameAndDeletedAtIsNull("Existing Product Name");
        verify(productRepository, never()).save(any(Product.class));
        verify(eventProducer, never()).publishProductUpdated(any(ProductDto.class));
    }

    @Test
    @DisplayName("Should not check for duplicate when name is not changed")
    void handle_ShouldNotCheckDuplicate_WhenNameUnchanged() {
        // Given
        command.setName("Original Name"); // Same as existing product name
        command.setDescription("Updated Description");
        when(productRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(productDto);
        doNothing().when(productCacheService).invalidateProduct(anyLong());
        doNothing().when(productCacheService).cacheProduct(any(ProductDto.class));
        doNothing().when(eventProducer).publishProductUpdated(any(ProductDto.class));

        // When
        handler.handle(command);

        // Then
        verify(productRepository, never()).existsByNameAndDeletedAtIsNull(anyString());
        verify(productRepository).save(product);
    }
}

