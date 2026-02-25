package com.hometask.inventory.dto;


public class OrderPlaceRequest {
    private String productId;
    private int quantity;

    public OrderPlaceRequest(String productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public OrderPlaceRequest(){}

    // Getters and Setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }


}
