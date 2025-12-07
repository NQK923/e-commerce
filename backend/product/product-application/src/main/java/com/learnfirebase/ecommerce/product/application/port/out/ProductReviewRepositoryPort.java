package com.learnfirebase.ecommerce.product.application.port.out;

import com.learnfirebase.ecommerce.common.application.pagination.PageRequest;
import com.learnfirebase.ecommerce.common.application.pagination.PageResponse;
import com.learnfirebase.ecommerce.product.application.dto.ProductReviewDto;
import com.learnfirebase.ecommerce.product.application.command.CreateReviewCommand;

public interface ProductReviewRepositoryPort {
    ProductReviewDto save(CreateReviewCommand command);
    PageResponse<ProductReviewDto> findByProductId(String productId, PageRequest pageRequest);
    boolean hasReviewed(String productId, String userId);
}
