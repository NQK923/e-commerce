package com.learnfirebase.ecommerce.promotion.domain.model;

public interface PromotionRule {
    boolean applies(double orderTotal);
}
