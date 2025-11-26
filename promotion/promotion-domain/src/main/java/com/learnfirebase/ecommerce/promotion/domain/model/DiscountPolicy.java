package com.learnfirebase.ecommerce.promotion.domain.model;

public interface DiscountPolicy {
    double apply(double orderTotal);
}
