package com.example.demo.exceptions;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String account, String email, String currentSellerEmail) {
    }

    public ResourceNotFoundException(String promotion, String id, Long promotionId) {
    }
}
