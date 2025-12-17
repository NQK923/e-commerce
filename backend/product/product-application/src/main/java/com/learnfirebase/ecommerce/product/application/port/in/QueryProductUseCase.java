package com.learnfirebase.ecommerce.product.application.port.in;

import java.util.List;

import com.learnfirebase.ecommerce.common.application.pagination.PageRequest;
import com.learnfirebase.ecommerce.common.application.pagination.PageResponse;
import com.learnfirebase.ecommerce.product.application.dto.ProductDto;
import com.learnfirebase.ecommerce.product.application.dto.ProductSearchQuery;

public interface QueryProductUseCase extends UseCase {
    PageResponse<ProductDto> searchProducts(ProductSearchQuery query, PageRequest pageRequest);

    PageResponse<ProductDto> listProducts(PageRequest pageRequest);

    ProductDto getProduct(String id);

    List<ProductDto> suggestProducts(String prefix, int limit);

    List<ProductDto> similarProducts(String productId, int limit);
}
