package com.hometask.inventory.dto;

public record AuthResponse(String token, String tokenType) {

    public AuthResponse(String token) {
        this(token, "Bearer");
    }
}