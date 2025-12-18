package com.learnfirebase.ecommerce.product.infrastructure.persistence;

import org.springframework.stereotype.Component;

import com.learnfirebase.ecommerce.product.application.port.out.PurchaseVerificationPort;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
public class PurchaseVerificationAdapter implements PurchaseVerificationPort {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public boolean hasCompletedPurchase(String userId, String productId) {
        if (userId == null || productId == null) {
            return false;
        }
        String sql = """
            SELECT COUNT(*) FROM orders o
            JOIN order_items oi ON o.id = oi.order_id
            WHERE o.user_id = :userId
            AND oi.product_id = :productId
            AND o.status IN ('PAID', 'COMPLETED')
            """;
        Number count = (Number) entityManager.createNativeQuery(sql)
            .setParameter("userId", userId)
            .setParameter("productId", productId)
            .getSingleResult();
        return count != null && count.longValue() > 0;
    }
}
