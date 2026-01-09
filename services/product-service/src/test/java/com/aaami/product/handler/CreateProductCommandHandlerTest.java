package com.aaami.product.handler;

import com.aaami.shared.command.CreateProductCommand;
import com.aaami.shared.dto.ProductDto;
import com.aaami.product.domain.Product;
import com.aaami.product.mapper.ProductMapper;
import com.aaami.product.repository.ProductRepository;
import com.aaami.product.service.ProductCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateProductCommandHandlerTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductCacheService productCacheService;

    @InjectMocks
    private CreateProductCommandHandler handler;

    private CreateProductCommand command;
    private Product product;
    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        command = new CreateProductCommand();
        command.setName("Test Product");
        command.setDescription("Test Description");
        command.setPrice(new BigDecimal("99.99"));
        command.setQuantity(100);

        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .quantity(100)
                .build();

        productDto = ProductDto.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .quantity(100)
                .build();
    }

    @Test
    void handle_ShouldCreateProduct_WhenCommandIsValid() {
        // Given
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toDto(any(Product.class))).thenReturn(productDto);
        doNothing().when(productCacheService).cacheProduct(any(ProductDto.class));

        // When
        ProductDto result = handler.handle(command);

        // Then
        assertNotNull(result);
        assertEquals(productDto.getId(), result.getId());
        assertEquals(productDto.getName(), result.getName());
        verify(productRepository).save(any(Product.class));
        verify(productMapper).toDto(any(Product.class));
    }

    @Test
    void handle_ShouldMapAllFieldsCorrectly() {
        // Given
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toDto(any(Product.class))).thenReturn(productDto);
        doNothing().when(productCacheService).cacheProduct(any(ProductDto.class));

        // When
        handler.handle(command);

        // Then
        verify(productRepository).save(argThat(p ->
                p.getName().equals(command.getName()) &&
                p.getDescription().equals(command.getDescription()) &&
                p.getPrice().equals(command.getPrice()) &&
                p.getQuantity().equals(command.getQuantity())
        ));
    }
}

