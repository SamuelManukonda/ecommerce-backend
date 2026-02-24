package com.hometask.ecommerce.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hometask.ecommerce.dto.PaginatedResponse;
import com.hometask.ecommerce.dto.ProductDto;
import com.hometask.ecommerce.dto.ProductMapper;
import com.hometask.ecommerce.model.Product;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;


import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Service
public class ProductService {

    private static final String REDIS_KEY = "product_list";

    private final ObjectMapper objectMapper;

    private final RedisTemplate<String, Product> redisTemplate;

    public ProductService(ObjectMapper objectMapper, RedisTemplate<String, Product> redisTemplate) {
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
        // Load products on startup
        loadProductsToRedis();
    }

    /**
     * Loads all products from static json file to redis during bean creation
     */
    private void loadProductsToRedis() {
        // Check if data exists in Redis
        Long size = redisTemplate.opsForList().size(REDIS_KEY);
        if (size != null && size > 0) {
            return; // Data already exists
        }

        try {
            ClassPathResource resource = new ClassPathResource("products.json");
            List<Product> products = objectMapper.readValue(resource.getInputStream(),
                    new TypeReference<List<Product>>() {
                    });

            if (products != null) {
                if (!products.isEmpty()) {
                    redisTemplate.opsForList().rightPushAll(REDIS_KEY, products);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load products from JSON file to Redis", e);
        }
    }

    /**
     * Get all paginated response products
     *
     * @param page current page number
     * @param size total elements in page
     * @return return Products
     */
    public PaginatedResponse<Product> getProducts(int page, int size) {
        Long totalElements = redisTemplate.opsForList().size(REDIS_KEY);
        if (totalElements == null || totalElements == 0) {
            return new PaginatedResponse<>(Collections.emptyList(), page, size, 0, 0);
        }

        int totalPages = (int) Math.ceil((double) totalElements / size);

        int start = page * size;
        int end = start + size - 1; // Redis range is inclusive

        // range returns List<Product> directly!
        List<Product> content = redisTemplate.opsForList().range(REDIS_KEY, start, end);

        if (content == null) {
            content = Collections.emptyList();
        }

        return new PaginatedResponse<>(content, page, size, totalElements, totalPages);
    }

    /**
     * Gets all products
     *
     * @return returns all products present in redis
     */
    public List<ProductDto> getAllProducts() {
        // Fetch all products
        List<Product> content = redisTemplate.opsForList().range(REDIS_KEY, 0, -1);
        return ProductMapper.toDtoList(content);
    }
}
