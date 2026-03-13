package com.hometask.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hometask.inventory.config.SecurityConfig;
import com.hometask.inventory.controller.AuthController;
import com.hometask.inventory.dto.AuthRequest;
import com.hometask.inventory.security.JwtAuthenticationFilter;
import com.hometask.inventory.security.JwtService;
import com.hometask.inventory.security.RestAuthenticationEntryPoint;
import com.hometask.inventory.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtService.class, RestAuthenticationEntryPoint.class, AuthService.class})
@TestPropertySource(properties = {
        "app.security.user.username=test-user",
        "app.security.user.password=test-password",
        "security.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "security.jwt.expiration-ms=3600000"
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_withValidCredentials_returnsJwtToken() throws Exception {
        AuthRequest request = new AuthRequest("test-user", "test-password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void login_withInvalidCredentials_returnsUnauthorized() throws Exception {
        AuthRequest request = new AuthRequest("test-user", "wrong-password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }
}

