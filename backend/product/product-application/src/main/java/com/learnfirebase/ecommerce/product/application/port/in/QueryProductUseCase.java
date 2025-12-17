package com.learnfirebase.ecommerce.product.application.port.in;

import java.util.List;

import com.learnfirebase.ecommerce.common.application.UseCase;
import com.learnfirebase.ecommerce.common.application.pagination.PageRequest;
import com.learnfirebase.ecommerce.common.application.pagination.PageResponse;
import com.learnfirebase.ecommerce.product.application.dto.ProductDto;
import com.learnfirebase.ecommerce.product.application.dto.ProductSearchQuery;
import com.learnfirebase.ecommerce.product.application.dto.ProductSearchWithFacetsDto;

public interface QueryProductUseCase extends UseCase {
    PageResponse<ProductDto> searchProducts(ProductSearchQuery query, PageRequest pageRequest);

    ProductSearchWithFacetsDto searchProductsAdvanced(ProductSearchQuery query, PageRequest pageRequest);

    PageResponse<ProductDto> listProducts(PageRequest pageRequest);

    ProductDto getProduct(String id);

    List<String> suggestProducts(String prefix, int limit);

    List<ProductDto> similarProducts(String productId, int limit);
}
