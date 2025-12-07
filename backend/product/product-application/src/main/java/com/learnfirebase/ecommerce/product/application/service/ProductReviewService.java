package com.learnfirebase.ecommerce.product.application.service;

import org.springframework.stereotype.Service;

import com.learnfirebase.ecommerce.common.application.pagination.PageRequest;
import com.learnfirebase.ecommerce.common.application.pagination.PageResponse;
import com.learnfirebase.ecommerce.product.application.command.CreateReviewCommand;
import com.learnfirebase.ecommerce.product.application.dto.ProductReviewDto;
import com.learnfirebase.ecommerce.product.application.port.out.ProductReviewRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductReviewService {
    private final ProductReviewRepositoryPort reviewRepository;

    public ProductReviewDto createReview(CreateReviewCommand command) {
        // Ideally verify purchase here by calling Order module (omitted for MVP)
        if (reviewRepository.hasReviewed(command.getProductId(), command.getUserId())) {
            throw new IllegalArgumentException("User has already reviewed this product");
        }
        return reviewRepository.save(command);
    }

    public PageResponse<ProductReviewDto> getProductReviews(String productId, int page, int size) {
        return reviewRepository.findByProductId(productId, PageRequest.builder().page(page).size(size).build());
    }
}
