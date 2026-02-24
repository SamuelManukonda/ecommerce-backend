package com.hometask.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hometask.inventory.dto.PaginatedResponse;
import com.hometask.inventory.dto.ProductDto;
import com.hometask.inventory.model.Product;
import com.hometask.inventory.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class ProductServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RedisTemplate<String, Product> redisTemplate;

    @Mock
    private ListOperations<String, Product> listOperations;

    private ProductService productService;

    private Product sampleProduct() {
        return new Product(
                "1",
                "Test Product",
                "A sample product",
                new BigDecimal("19.99"),
                "USD",
                "Electronics",
                100,
                "http://example.com/image.jpg",
                new BigDecimal("4.5")
        );
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        productService = new ProductService(objectMapper, redisTemplate); // Manua
    }

    @Test
    void getAllProducts_returnsProducts() {
        List<Product> products = Arrays.asList(sampleProduct());
        when(listOperations.range("product_list", 0, -1)).thenReturn(products);

        List<ProductDto> result = productService.getAllProducts();

        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());
        assertEquals("USD", result.get(0).getCurrency());
        assertEquals(new BigDecimal("4.5"), result.get(0).getRating());
    }

    @Test
    void getAllProducts_returnsEmptyListWhenNull() {
        when(listOperations.range("product_list", 0, -1)).thenReturn(null);

        List<ProductDto> result = productService.getAllProducts();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getProducts_returnsPaginatedResponse() {
        List<Product> products = Arrays.asList(sampleProduct());
        when(listOperations.size("product_list")).thenReturn(1L);
        when(listOperations.range("product_list", 0, 0)).thenReturn(products);

        PaginatedResponse<Product> response = productService.getProducts(0, 1);

        assertEquals(1, response.getContent().size());
        assertEquals(1, response.getTotalPages());
        assertEquals(1, response.getTotalElements());
        assertEquals("Test Product", response.getContent().get(0).getName());
    }

    @Test
    void getProducts_returnsEmptyWhenNoData() {
        when(listOperations.size("product_list")).thenReturn(0L);

        PaginatedResponse<Product> response = productService.getProducts(0, 2);

        assertNotNull(response);
        assertTrue(response.getContent().isEmpty());
        assertEquals(0, response.getTotalElements());
        assertEquals(0, response.getTotalPages());
    }

}
