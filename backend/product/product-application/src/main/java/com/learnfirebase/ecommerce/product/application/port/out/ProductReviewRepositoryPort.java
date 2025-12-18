package com.learnfirebase.ecommerce.product.application.port.out;

import com.learnfirebase.ecommerce.common.application.pagination.PageRequest;
import com.learnfirebase.ecommerce.common.application.pagination.PageResponse;
import java.time.Instant;
import java.util.Optional;

import com.learnfirebase.ecommerce.product.application.dto.ProductReviewDto;
import com.learnfirebase.ecommerce.product.application.command.RespondReviewCommand;
import com.learnfirebase.ecommerce.product.application.command.CreateReviewCommand;
import com.learnfirebase.ecommerce.product.application.command.UpdateReviewCommand;

public interface ProductReviewRepositoryPort {
    ProductReviewDto save(CreateReviewCommand command);
    ProductReviewDto update(UpdateReviewCommand command);
    void delete(String reviewId);
    ProductReviewDto respond(RespondReviewCommand command);
    PageResponse<ProductReviewDto> findByProductId(String productId, PageRequest pageRequest);
    boolean hasReviewed(String productId, String userId);
    long countRecentReviews(String userId, Instant createdAfter);
    Optional<ProductReviewDto> findById(String reviewId);
    void incrementAbuseReport(String reviewId);
}
