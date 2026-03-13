package com.hometask.inventory;


import com.hometask.inventory.config.SecurityConfig;
import com.hometask.inventory.controller.ProductController;
import com.hometask.inventory.dto.PaginatedResponse;
import com.hometask.inventory.dto.ProductDto;
import com.hometask.inventory.model.Product;
import com.hometask.inventory.security.JwtAuthenticationFilter;
import com.hometask.inventory.security.JwtService;
import com.hometask.inventory.security.RestAuthenticationEntryPoint;
import com.hometask.inventory.service.ProductService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtService.class, RestAuthenticationEntryPoint.class})
@TestPropertySource(properties = {
        "app.security.user.username=test-user",
        "app.security.user.password=test-password",
        "security.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "security.jwt.expiration-ms=3600000"
})
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @MockitoBean
    private ProductService productService;

    private String bearerToken() {
        String token = jwtService.generateToken(userDetailsService.loadUserByUsername("test-user"));
        return "Bearer " + token;
    }

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

    private ProductDto sampleProductDto() {
        return new ProductDto(
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
                List.of(sampleProduct()),
                0, 10, 1, 1
        );
        Mockito.when(productService.getProducts(anyInt(), anyInt())).thenReturn(response);

        mockMvc.perform(get("/api/products?page=0&size=10")
                        .header("Authorization", bearerToken()))
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
                .thenReturn(List.of(sampleProductDto()));

        mockMvc.perform(get("/api/products/all")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Product"))
                .andExpect(jsonPath("$[0].currency").value("USD"))
                .andExpect(jsonPath("$[0].rating").value(4.5));
    }

    @Test
    void getAllProducts_integration() throws Exception {
        ProductDto dto = sampleProductDto();
        Mockito.when(productService.getAllProducts()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/products/all")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(dto.getId()))
                .andExpect(jsonPath("$[0].name").value(dto.getName()));
    }

    @Test
    void getProducts_invalidPage_throwsException() throws Exception {
        mockMvc.perform(get("/api/products?page=-1&size=10")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getProducts_invalidSize_throwsException() throws Exception {
        mockMvc.perform(get("/api/products?page=0&size=0")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isBadRequest());
    }

  /*  @Test
    void getAllProducts_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/products/all"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }*/
}