package com.learnfirebase.ecommerce.product.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.nio.BasicHttpAsyncClientBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
import com.learnfirebase.ecommerce.product.infrastructure.search.ElasticsearchProperties;

@Configuration
@EnableJpaRepositories(basePackageClasses = ProductJpaRepository.class)
@EntityScan(basePackageClasses = ProductEntity.class)
@EnableConfigurationProperties(ElasticsearchProperties.class)
public class ProductModuleConfig {
    @Bean
    public RestClient elasticsearchRestClient(ElasticsearchProperties properties) {
        RestClient.Builder builder = RestClient.builder(HttpHost.create(properties.getHost()));
        if (properties.getUsername() != null && !properties.getUsername().isBlank()) {
            BasicCredentialsProvider creds = new BasicCredentialsProvider();
            creds.setCredentials(AuthScope.ANY, new org.apache.hc.client5.http.auth.UsernamePasswordCredentials(properties.getUsername(), properties.getPassword().toCharArray()));
            builder.setHttpClientConfigCallback((BasicHttpAsyncClientBuilder httpClientBuilder) ->
                httpClientBuilder.setDefaultCredentialsProvider(creds)
            );
        }
        return builder.build();
    }

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
