package com.learnfirebase.ecommerce.product.application.port.out;

public interface PurchaseVerificationPort {
    boolean hasCompletedPurchase(String userId, String productId);
}
