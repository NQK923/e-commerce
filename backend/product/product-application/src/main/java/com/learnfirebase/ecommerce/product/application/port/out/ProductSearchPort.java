package com.learnfirebase.ecommerce.product.application.port.out;

import java.util.List;

import com.learnfirebase.ecommerce.common.application.pagination.PageRequest;
import com.learnfirebase.ecommerce.product.application.dto.ProductSearchQuery;
import com.learnfirebase.ecommerce.product.application.dto.ProductSearchResult;
import com.learnfirebase.ecommerce.product.domain.model.Product;

public interface ProductSearchPort {
    ProductSearchResult search(ProductSearchQuery query, PageRequest pageRequest);
    List<String> suggest(String prefix, int limit);
    List<Product> similarProducts(String productId, int limit);
}
