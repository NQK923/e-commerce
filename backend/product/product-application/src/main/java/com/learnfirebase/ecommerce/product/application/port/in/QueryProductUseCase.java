package com.learnfirebase.ecommerce.product.application.port.in;

import com.learnfirebase.ecommerce.common.application.UseCase;
import com.learnfirebase.ecommerce.common.application.pagination.PageRequest;
import com.learnfirebase.ecommerce.common.application.pagination.PageResponse;
import com.learnfirebase.ecommerce.product.application.dto.ProductDto;

import com.learnfirebase.ecommerce.product.application.dto.ProductSearchQuery;

public interface QueryProductUseCase extends UseCase {
    PageResponse<ProductDto> listProducts(PageRequest pageRequest);
    
    PageResponse<ProductDto> searchProducts(ProductSearchQuery query, PageRequest pageRequest);

    ProductDto getProduct(String id);
}
