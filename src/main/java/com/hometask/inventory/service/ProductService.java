package com.hometask.inventory.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hometask.inventory.dto.PaginatedResponse;
import com.hometask.inventory.dto.ProductDto;
import com.hometask.inventory.dto.ProductMapper;
import com.hometask.inventory.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final static Logger LOGGER = LoggerFactory.getLogger(ProductService.class);

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

    /**
     * Decrements the stock quantity of a product in Redis
     *
     * @param productId the ID of the product
     * @param quantity  the quantity to decrement
     * @return true if successful, false if product not found or insufficient stock
     */
    public boolean decrementProductQuantity(String productId, int quantity) {
        List<Product> products = redisTemplate.opsForList().range(REDIS_KEY, 0, -1);

        if (products == null || products.isEmpty()) {
            LOGGER.error("No products found in Redis");
            return false;
        }

        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            if (product.getId().equals(productId)) {
                // Check if sufficient stock exists
                if (product.getStock() < quantity) {
                    System.err.println("Insufficient stock for product " + productId +
                            ". Available: " + product.getStock() + ", Requested: " + quantity);
                    return false;
                }

                // Decrement the stock
                product.setStock(product.getStock() - quantity);

                // Update the product in Redis at the same index
                redisTemplate.opsForList().set(REDIS_KEY, i, product);

                LOGGER.info("Successfully decremented stock for product {}. New stock: {}", productId, product.getStock());
                return true;
            }
        }

        LOGGER.error("Product not found: {}", productId);
        return false;
    }
}
