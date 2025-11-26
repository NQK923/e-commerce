package com.learnfirebase.ecommerce.product.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.learnfirebase.ecommerce.product.application.port.out.ProductEventPublisher;
import com.learnfirebase.ecommerce.product.application.port.out.ProductRepository;
import com.learnfirebase.ecommerce.product.application.port.out.ProductSearchIndexPort;
import com.learnfirebase.ecommerce.product.application.service.ProductApplicationService;

@Configuration
public class ProductModuleConfig {
    @Bean
    public ProductApplicationService productApplicationService(ProductRepository productRepository, ProductSearchIndexPort productSearchIndexPort, ProductEventPublisher productEventPublisher) {
        return new ProductApplicationService(productRepository, productSearchIndexPort, productEventPublisher);
    }
}
