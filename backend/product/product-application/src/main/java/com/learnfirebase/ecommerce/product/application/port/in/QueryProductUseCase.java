package com.learnfirebase.ecommerce.product.application.port.in;

import com.learnfirebase.ecommerce.common.application.UseCase;
import com.learnfirebase.ecommerce.common.application.pagination.PageRequest;
import com.learnfirebase.ecommerce.common.application.pagination.PageResponse;
import com.learnfirebase.ecommerce.product.application.dto.ProductDto;

public interface QueryProductUseCase extends UseCase {
    PageResponse<ProductDto> listProducts(PageRequest pageRequest);

    ProductDto getProduct(String id);
}
