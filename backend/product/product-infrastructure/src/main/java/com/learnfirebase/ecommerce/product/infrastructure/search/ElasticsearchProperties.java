package com.learnfirebase.ecommerce.product.infrastructure.search;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "search.elasticsearch")
public class ElasticsearchProperties {

    private String host = "http://localhost:9200";
    private String username;
    private String password;
    private String index = "products";
}
