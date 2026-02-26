package com.hometask.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hometask.inventory.dto.OrderPlaceRequest;
import com.hometask.inventory.model.Product;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InventoryIntegrationTest {

    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.7"));
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:latest")).withExposedPorts(6379);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        if (!kafka.isRunning()) kafka.start();
        if (!redis.isRunning()) redis.start();
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

    @BeforeEach
    void setupProduct() {
        redisTemplate.delete("product_list");
        Product product = new Product();
        product.setId("3");
        product.setName("Test Product");
        product.setStock(10);
        redisTemplate.opsForList().leftPush("product_list", product);
    }

    @Test
    void inventoryUpdateListener_decrementsStock() throws Exception {
        int initialStock = 10;
        OrderPlaceRequest request = new OrderPlaceRequest("3", 2);
        kafkaTemplate.send("inventory-updates", objectMapper.convertValue(request, java.util.Map.class));

        Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            List<Product> updatedProducts = redisTemplate.opsForList().range("product_list", 0, -1);
            Product updatedProduct = updatedProducts.stream().filter(p -> p.getId().equals("3")).findFirst().orElse(null);
            assertNotNull(updatedProduct);
            assertEquals(initialStock - 2, updatedProduct.getStock());
        });
    }
}