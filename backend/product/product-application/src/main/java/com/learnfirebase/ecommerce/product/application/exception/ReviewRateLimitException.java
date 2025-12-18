package com.learnfirebase.ecommerce.product.application.exception;

public class ReviewRateLimitException extends RuntimeException {
    public ReviewRateLimitException(String message) {
        super(message);
    }
}
