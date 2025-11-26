package com.learnfirebase.ecommerce.product.infrastructure.search;

import org.springframework.stereotype.Component;

import com.learnfirebase.ecommerce.product.application.port.out.ProductSearchIndexPort;
import com.learnfirebase.ecommerce.product.domain.model.Product;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ElasticsearchProductIndexAdapter implements ProductSearchIndexPort {
    @Override
    public void index(Product product) {
        log.info("Indexing product {} with {} image(s)", product.getId().getValue(),
            product.getImages() != null ? product.getImages().size() : 0);
    }
}
