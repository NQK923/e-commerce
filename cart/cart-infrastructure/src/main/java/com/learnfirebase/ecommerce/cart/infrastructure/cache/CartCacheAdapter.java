package com.learnfirebase.ecommerce.cart.infrastructure.cache;

import org.springframework.stereotype.Component;

import com.learnfirebase.ecommerce.cart.application.port.out.CartCachePort;
import com.learnfirebase.ecommerce.cart.domain.model.Cart;

@Component
public class CartCacheAdapter implements CartCachePort {
    @Override
    public void cache(Cart cart) {
        // store in Redis in a full implementation
    }
}
