package com.learnfirebase.ecommerce.cart.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.learnfirebase.ecommerce.cart.application.port.out.CartCachePort;
import com.learnfirebase.ecommerce.cart.application.port.out.CartRepository;
import com.learnfirebase.ecommerce.cart.application.service.CartApplicationService;

@Configuration
public class CartModuleConfig {
    @Bean
    public CartApplicationService cartApplicationService(CartRepository cartRepository, CartCachePort cartCachePort) {
        return new CartApplicationService(cartRepository, cartCachePort);
    }
}
