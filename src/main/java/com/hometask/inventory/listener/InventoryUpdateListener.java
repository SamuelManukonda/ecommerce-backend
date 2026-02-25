package com.hometask.inventory.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hometask.inventory.dto.OrderPlaceRequest;
import com.hometask.inventory.service.ProductService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InventoryUpdateListener {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ProductService productService;

    public InventoryUpdateListener(ProductService productService) {
        this.productService = productService;
    }

    @KafkaListener(topics = "inventory-updates", groupId = "inventory-group")
    public void handleInventoryUpdate(Map<String, Object> payload) {
        try {
            OrderPlaceRequest request = objectMapper.convertValue(payload, OrderPlaceRequest.class);

            System.out.println("Received inventory update request - Product ID: " +
                             request.getProductId() + ", Quantity: " + request.getQuantity());

            // Decrement the product quantity in Redis
            boolean success = productService.decrementProductQuantity(
                request.getProductId(),
                request.getQuantity()
            );

            if (success) {
                System.out.println("Successfully processed inventory update for product: " +
                                 request.getProductId());
            } else {
                System.err.println("Failed to process inventory update for product: " +
                                 request.getProductId());
            }

        } catch (Exception e) {
            System.err.println("Error processing inventory update: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

