package com.learnfirebase.ecommerce.cart.application.port.out;

import com.learnfirebase.ecommerce.cart.domain.model.Cart;

public interface CartCachePort {
    void cache(Cart cart);
}
