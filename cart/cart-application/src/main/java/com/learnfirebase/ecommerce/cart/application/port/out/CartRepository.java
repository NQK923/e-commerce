package com.learnfirebase.ecommerce.cart.application.port.out;

import java.util.Optional;

import com.learnfirebase.ecommerce.cart.domain.model.Cart;
import com.learnfirebase.ecommerce.cart.domain.model.CartId;

public interface CartRepository {
    Cart save(Cart cart);

    Optional<Cart> findById(CartId id);
}
