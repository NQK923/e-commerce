package com.learnfirebase.ecommerce.product.application.service;

import java.time.Duration;
import java.time.Instant;

import com.learnfirebase.ecommerce.common.application.pagination.PageRequest;
import com.learnfirebase.ecommerce.common.application.pagination.PageResponse;
import com.learnfirebase.ecommerce.product.application.command.CreateReviewCommand;
import com.learnfirebase.ecommerce.product.application.command.DeleteReviewCommand;
import com.learnfirebase.ecommerce.product.application.command.ReportReviewCommand;
import com.learnfirebase.ecommerce.product.application.command.RespondReviewCommand;
import com.learnfirebase.ecommerce.product.application.command.UpdateReviewCommand;
import com.learnfirebase.ecommerce.product.application.dto.ProductReviewDto;
import com.learnfirebase.ecommerce.product.application.exception.ReviewRateLimitException;
import com.learnfirebase.ecommerce.product.application.port.out.ProductRepository;
import com.learnfirebase.ecommerce.product.application.port.out.ProductReviewReportRepositoryPort;
import com.learnfirebase.ecommerce.product.application.port.out.ProductReviewRepositoryPort;
import com.learnfirebase.ecommerce.product.application.port.out.PurchaseVerificationPort;
import com.learnfirebase.ecommerce.product.domain.model.ProductId;
import com.learnfirebase.ecommerce.product.domain.model.ReportReason;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProductReviewService {
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofMinutes(5);
    private static final int MAX_REVIEWS_PER_WINDOW = 3;

    private final ProductReviewRepositoryPort reviewRepository;
    private final ProductReviewReportRepositoryPort reviewReportRepository;
    private final PurchaseVerificationPort purchaseVerificationPort;
    private final ProductRepository productRepository;

    public ProductReviewDto createReview(CreateReviewCommand command) {
        validateRating(command.getRating());
        ensureProductExists(command.getProductId());
        enforceRateLimit(command.getUserId());
        if (reviewRepository.hasReviewed(command.getProductId(), command.getUserId())) {
            throw new IllegalArgumentException("User has already reviewed this product");
        }
        boolean verifiedPurchase = purchaseVerificationPort.hasCompletedPurchase(command.getUserId(), command.getProductId());
        if (!verifiedPurchase) {
            throw new IllegalArgumentException("Only verified buyers can review this product");
        }
        command.setVerifiedPurchase(verifiedPurchase);
        return reviewRepository.save(command);
    }

    public ProductReviewDto updateReview(UpdateReviewCommand command) {
        validateRating(command.getRating());
        ProductReviewDto existing = getReview(command.getReviewId());
        ensureSameProduct(existing.getProductId(), command.getProductId());
        ensureOwner(existing.getUserId(), command.getUserId());
        return reviewRepository.update(command);
    }

    public void deleteReview(DeleteReviewCommand command) {
        ProductReviewDto existing = getReview(command.getReviewId());
        ensureSameProduct(existing.getProductId(), command.getProductId());
        ensureOwner(existing.getUserId(), command.getUserId());
        reviewRepository.delete(command.getReviewId());
    }

    public void reportAbuse(ReportReviewCommand command) {
        ProductReviewDto review = getReview(command.getReviewId());
        ensureSameProduct(review.getProductId(), command.getProductId());
        if (command.getReporterUserId() == null || command.getReporterUserId().isBlank()) {
            throw new IllegalArgumentException("Reporter id is required to flag a review");
        }
        ReportReason reason = command.getReason() == null ? ReportReason.OTHER : command.getReason();
        command.setReason(reason);
        if (reviewReportRepository.hasReported(command.getReviewId(), command.getReporterUserId())) {
            throw new IllegalStateException("User has already reported this review");
        }
        reviewReportRepository.saveReport(command);
        reviewRepository.incrementAbuseReport(command.getReviewId());
    }

    public ProductReviewDto respondToReview(RespondReviewCommand command) {
        ProductReviewDto review = getReview(command.getReviewId());
        ensureSameProduct(review.getProductId(), command.getProductId());
        if (command.getResponse() == null || command.getResponse().isBlank()) {
            throw new IllegalArgumentException("Response cannot be empty");
        }
        String sellerId = resolveSellerId(review.getProductId());
        if (sellerId == null || !sellerId.equals(command.getSellerId())) {
            throw new IllegalArgumentException("Only the product seller can respond to reviews");
        }
        return reviewRepository.respond(command);
    }

    public PageResponse<ProductReviewDto> getProductReviews(String productId, int page, int size) {
        return reviewRepository.findByProductId(productId, PageRequest.builder().page(page).size(size).build());
    }

    private void enforceRateLimit(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User id is required for review actions");
        }
        Instant since = Instant.now().minus(RATE_LIMIT_WINDOW);
        long recent = reviewRepository.countRecentReviews(userId, since);
        if (recent >= MAX_REVIEWS_PER_WINDOW) {
            throw new ReviewRateLimitException("Too many reviews in a short period. Please wait before posting again.");
        }
    }

    private ProductReviewDto getReview(String reviewId) {
        return reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("Review not found"));
    }

    private void ensureSameProduct(String existingProductId, String requestedProductId) {
        if (requestedProductId != null && !requestedProductId.equals(existingProductId)) {
            throw new IllegalArgumentException("Review does not belong to the specified product");
        }
    }

    private void ensureOwner(String ownerId, String requesterId) {
        if (requesterId == null || !requesterId.equals(ownerId)) {
            throw new IllegalArgumentException("Only the review owner can modify this review");
        }
    }

    private void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }

    private void ensureProductExists(String productId) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product id is required");
        }
        productRepository.findById(new ProductId(productId))
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }

    private String resolveSellerId(String productId) {
        if (productId == null || productId.isBlank()) {
            return null;
        }
        return productRepository.findById(new ProductId(productId))
            .map(p -> p.getSellerId())
            .orElse(null);
    }
}
