package com.aaami.gateway.client;

import com.aaami.gateway.config.ServiceProperties;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
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
    @DisplayName("Should create product successfully")
    void createProduct_ShouldReturnProductDto() {
        // Given
        CreateProductCommand command = new CreateProductCommand();
        ResponseEntity<ProductDto> response = new ResponseEntity<>(productDto, HttpStatus.CREATED);
        when(restTemplate.postForEntity(anyString(), any(CreateProductCommand.class), eq(ProductDto.class)))
                .thenReturn(response);

        // When
        ProductDto result = client.createProduct(command);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(restTemplate).postForEntity("http://localhost:8081/api/products", command, ProductDto.class);
    }

    @Test
    @DisplayName("Should get product by ID")
    void getProduct_ShouldReturnProductDto() {
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
        ResponseEntity<PaginatedResponse<ProductDto>> response = new ResponseEntity<>(paginatedResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        // When
        PaginatedResponse<ProductDto> result = client.searchProducts(null, null, null, null, null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class));
    }

    @Test
    @DisplayName("Should update product successfully")
    void updateProduct_ShouldReturnUpdatedProductDto() {
        // Given
        UpdateProductCommand command = new UpdateProductCommand();
        ResponseEntity<ProductDto> response = new ResponseEntity<>(productDto, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(ProductDto.class)))
                .thenReturn(response);

        // When
        ProductDto result = client.updateProduct(1L, command);

        // Then
        assertNotNull(result);
        verify(restTemplate).exchange(
                eq("http://localhost:8081/api/products/1"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(ProductDto.class)
        );
    }

    @Test
    @DisplayName("Should delete product successfully")
    void deleteProduct_ShouldCallDeleteEndpoint() {
        // When
        client.deleteProduct(1L);

        // Then
        verify(restTemplate).delete("http://localhost:8081/api/products/1");
    }
}

