package com.hometask.inventory.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hometask.inventory.dto.OrderPlaceRequest;
import com.hometask.inventory.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InventoryUpdateListener {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ProductService productService;
    public static final Logger LOGGER = LoggerFactory.getLogger(InventoryUpdateListener.class);

    public InventoryUpdateListener(ProductService productService) {
        this.productService = productService;
    }

    @KafkaListener(topics = "inventory-updates", groupId = "inventory-group")
    public void handleInventoryUpdate(Map<String, Object> payload) {
        try {
            OrderPlaceRequest request = objectMapper.convertValue(payload, OrderPlaceRequest.class);

            LOGGER.debug("Received inventory update request - Product ID: {}, Quantity: {}", request.getProductId(), request.getQuantity());

            // Decrement the product quantity in Redis
            boolean success = productService.decrementProductQuantity(
                    request.getProductId(),
                    request.getQuantity()
            );

            if (success) {
                LOGGER.info("Successfully processed inventory update for product: {}", request.getProductId());
            }

        } catch (Exception e) {
            LOGGER.error("Error processing inventory update: {}", e.getMessage());
        }
    }
}

