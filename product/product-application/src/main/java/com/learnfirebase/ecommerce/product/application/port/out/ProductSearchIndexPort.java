package com.learnfirebase.ecommerce.product.application.port.out;

import com.learnfirebase.ecommerce.product.domain.model.Product;

public interface ProductSearchIndexPort {
    void index(Product product);
}
