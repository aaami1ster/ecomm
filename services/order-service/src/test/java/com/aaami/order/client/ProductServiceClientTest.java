package com.aaami.order.client;

import com.aaami.config.ServiceProperties;
import com.aaami.shared.command.DecreaseProductQuantityCommand;
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
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceClient Tests")
class ProductServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ServiceProperties serviceProperties;

    @InjectMocks
    private ProductServiceClient client;

    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        when(serviceProperties.getProductServiceUrl()).thenReturn("http://localhost:8081");

        productDto = ProductDto.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .quantity(10)
                .build();
    }

    @Test
    @DisplayName("Should get product by ID")
    void getProduct_ShouldReturnProduct_WhenProductExists() {
        // Given
        ResponseEntity<ProductDto> response = new ResponseEntity<>(productDto, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(ProductDto.class))).thenReturn(response);

        // When
        ProductDto result = client.getProduct(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(restTemplate).getForEntity("http://localhost:8081/api/products/1", ProductDto.class);
    }

    @Test
    @DisplayName("Should decrease product quantity")
    void decreaseProductQuantity_ShouldReturnUpdatedProduct() {
        // Given
        ProductDto updatedProduct = ProductDto.builder()
                .id(1L)
                .name("Test Product")
                .quantity(8) // Decreased from 10
                .build();
        ResponseEntity<ProductDto> response = new ResponseEntity<>(updatedProduct, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(ProductDto.class))).thenReturn(response);

        // When
        ProductDto result = client.decreaseProductQuantity(1L, 2);

        // Then
        assertNotNull(result);
        assertEquals(8, result.getQuantity());
        verify(restTemplate).postForEntity(
                eq("http://localhost:8081/api/products/1/decrease-quantity"),
                any(DecreaseProductQuantityCommand.class),
                eq(ProductDto.class)
        );
    }
}

