package com.hometask.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hometask.inventory.dto.OrderPlaceRequest;
import com.hometask.inventory.model.Product;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class InventoryIntegrationTest {

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.7"));

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:latest")).withExposedPorts(6379);

    static {
        kafka.start();
        redis.start();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private RedisTemplate<String, Product> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @BeforeEach
    void setupProduct() {
        redisTemplate.delete("product_list");
        Product product = new Product(
            "3",
            "Test Product",
            "Test Description",
            new BigDecimal("19.99"),
            "USD",
            "Electronics",
            10,
            "http://example.com/image.jpg",
            new BigDecimal("4.5")
        );
        redisTemplate.opsForList().leftPush("product_list", product);
    }

    @Test
    void inventoryUpdateListener_decrementsStock() throws Exception {
        // Wait for Kafka listener to be assigned partitions
        for (MessageListenerContainer container : kafkaListenerEndpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(container, 1);
        }

        int initialStock = 10;
        OrderPlaceRequest request = new OrderPlaceRequest("3", 2);
        kafkaTemplate.send("inventory-updates", objectMapper.convertValue(request, java.util.Map.class)).get();

        Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            List<Product> updatedProducts = redisTemplate.opsForList().range("product_list", 0, -1);
            Product updatedProduct = updatedProducts.stream().filter(p -> p.getId().equals("3")).findFirst().orElse(null);
            assertNotNull(updatedProduct);
            assertEquals(initialStock - 2, updatedProduct.getStock());
        });
    }
}