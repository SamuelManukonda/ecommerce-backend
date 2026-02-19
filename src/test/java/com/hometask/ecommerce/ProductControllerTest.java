package com.hometask.ecommerce;


import com.hometask.ecommerce.controller.ProductController;
import com.hometask.ecommerce.dto.PaginatedResponse;
import com.hometask.ecommerce.model.Product;
import com.hometask.ecommerce.service.ProductService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
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

    @Test
    void getProducts_returnsPaginatedResponse() throws Exception {
        PaginatedResponse<Product> response = new PaginatedResponse<>(
                Arrays.asList(sampleProduct()),
                0, 10, 1, 1
        );
        Mockito.when(productService.getProducts(anyInt(), anyInt())).thenReturn(response);

        mockMvc.perform(get("/api/products?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Test Product"))
                .andExpect(jsonPath("$.content[0].currency").value("USD"))
                .andExpect(jsonPath("$.content[0].rating").value(4.5))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    void getAllProducts_returnsList() throws Exception {
        Mockito.when(productService.getAllProducts())
                .thenReturn(Arrays.asList(sampleProduct()));

        mockMvc.perform(get("/api/products/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Product"))
                .andExpect(jsonPath("$[0].currency").value("USD"))
                .andExpect(jsonPath("$[0].rating").value(4.5));
    }

    @Test
    void getProducts_invalidPage_throwsException() throws Exception {
        mockMvc.perform(get("/api/products?page=-1&size=10"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getProducts_invalidSize_throwsException() throws Exception {
        mockMvc.perform(get("/api/products?page=0&size=0"))
                .andExpect(status().isInternalServerError());
    }
}