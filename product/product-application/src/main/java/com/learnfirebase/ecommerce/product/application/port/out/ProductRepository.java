package com.learnfirebase.ecommerce.product.application.port.out;

import java.util.Optional;

import com.learnfirebase.ecommerce.product.domain.model.Product;
import com.learnfirebase.ecommerce.product.domain.model.ProductId;

public interface ProductRepository {
    Product save(Product product);

    Optional<Product> findById(ProductId id);
}
