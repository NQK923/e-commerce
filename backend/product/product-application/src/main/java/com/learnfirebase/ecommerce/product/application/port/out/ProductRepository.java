package com.learnfirebase.ecommerce.product.application.port.out;

import java.util.Optional;

import com.learnfirebase.ecommerce.product.application.dto.ProductSearchQuery;
import com.learnfirebase.ecommerce.common.application.pagination.PageRequest;
import com.learnfirebase.ecommerce.common.application.pagination.PageResponse;
import com.learnfirebase.ecommerce.product.domain.model.Product;
import com.learnfirebase.ecommerce.product.domain.model.ProductId;

public interface ProductRepository {
    Product save(Product product);

    Optional<Product> findById(ProductId id);

    PageResponse<Product> findAll(PageRequest pageRequest);
    
    PageResponse<Product> search(ProductSearchQuery query, PageRequest pageRequest);

    void incrementSoldCount(ProductId id, int quantity);
}
