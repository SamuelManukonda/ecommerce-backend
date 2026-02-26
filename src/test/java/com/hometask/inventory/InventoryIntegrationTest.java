package com.hometask.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hometask.inventory.dto.OrderPlaceRequest;
import com.hometask.inventory.model.Product;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class InventoryIntegrationTest {

    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.7"));
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:latest")).withExposedPorts(6379);

    @BeforeAll
    static void startContainers() {
        kafka.start();
        redis.start();
    }

    @AfterAll
    static void stopContainers() {
        kafka.stop();
        redis.stop();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private RedisTemplate<String, Product> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void inventoryUpdateListener_decrementsStock() throws Exception {
        // Assume product with id "3" exists and has stock >= 2
        List<Product> products = redisTemplate.opsForList().range("product_list", 0, -1);
        Product product = products.stream().filter(p -> p.getId().equals("3")).findFirst().orElse(null);
        assertNotNull(product, "Product with id 3 should exist");
        int initialStock = product.getStock();
        assertTrue(initialStock >= 2, "Initial stock should be >= 2");

        OrderPlaceRequest request = new OrderPlaceRequest("3", 2);
        kafkaTemplate.send("inventory-updates", objectMapper.convertValue(request, java.util.Map.class));

        // Wait for listener to process
        Thread.sleep(5000);

        List<Product> updatedProducts = redisTemplate.opsForList().range("product_list", 0, -1);
        Product updatedProduct = updatedProducts.stream().filter(p -> p.getId().equals("3")).findFirst().orElse(null);
        assertNotNull(updatedProduct);
        assertEquals(initialStock - 2, updatedProduct.getStock());
    }
}

