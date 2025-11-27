package com.learnfirebase.ecommerce.cart.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.learnfirebase.ecommerce.cart.application.port.out.CartCachePort;
import com.learnfirebase.ecommerce.cart.application.port.out.CartRepository;
import com.learnfirebase.ecommerce.cart.application.service.CartApplicationService;
import com.learnfirebase.ecommerce.cart.infrastructure.persistence.CartEntity;
import com.learnfirebase.ecommerce.cart.infrastructure.persistence.CartJpaRepository;

@Configuration
@EnableJpaRepositories(basePackageClasses = CartJpaRepository.class)
@EntityScan(basePackageClasses = CartEntity.class)
public class CartModuleConfig {
    @Bean
    public CartApplicationService cartApplicationService(CartRepository cartRepository, CartCachePort cartCachePort) {
        return new CartApplicationService(cartRepository, cartCachePort);
    }
}
