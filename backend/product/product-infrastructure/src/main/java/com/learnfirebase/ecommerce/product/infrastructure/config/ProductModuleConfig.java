package com.learnfirebase.ecommerce.product.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.learnfirebase.ecommerce.product.application.port.out.ProductEventPublisher;
import com.learnfirebase.ecommerce.product.application.port.out.ProductReportRepository;
import com.learnfirebase.ecommerce.product.application.port.out.ProductRepository;
import com.learnfirebase.ecommerce.product.application.port.out.ProductReviewRepositoryPort;
import com.learnfirebase.ecommerce.product.application.port.out.ProductSearchIndexPort;
import com.learnfirebase.ecommerce.product.application.port.out.ProductSearchPort;
import com.learnfirebase.ecommerce.product.application.service.ProductApplicationService;
import com.learnfirebase.ecommerce.product.application.service.ProductReportApplicationService;
import com.learnfirebase.ecommerce.product.application.service.ProductReviewService;
import com.learnfirebase.ecommerce.product.infrastructure.persistence.ProductEntity;
import com.learnfirebase.ecommerce.product.infrastructure.persistence.ProductJpaRepository;

@Configuration
@EnableJpaRepositories(basePackageClasses = ProductJpaRepository.class)
@EntityScan(basePackageClasses = ProductEntity.class)
public class ProductModuleConfig {
    @Bean
    public ProductApplicationService productApplicationService(ProductRepository productRepository, ProductSearchIndexPort productSearchIndexPort, ProductSearchPort productSearchPort, ProductEventPublisher productEventPublisher) {
        return new ProductApplicationService(productRepository, productSearchIndexPort, productSearchPort, productEventPublisher);
    }

    @Bean
    public ProductReportApplicationService productReportApplicationService(ProductReportRepository productReportRepository) {
        return new ProductReportApplicationService(productReportRepository);
    }

    @Bean
    public ProductReviewService productReviewService(ProductReviewRepositoryPort productReviewRepositoryPort) {
        return new ProductReviewService(productReviewRepositoryPort);
    }
}
