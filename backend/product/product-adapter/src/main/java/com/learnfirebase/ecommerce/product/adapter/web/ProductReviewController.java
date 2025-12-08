package com.learnfirebase.ecommerce.product.adapter.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learnfirebase.ecommerce.common.application.pagination.PageResponse;
import com.learnfirebase.ecommerce.product.application.command.CreateReviewCommand;
import com.learnfirebase.ecommerce.product.application.dto.ProductReviewDto;
import com.learnfirebase.ecommerce.product.application.service.ProductReviewService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductReviewController {
    private final ProductReviewService reviewService;

    @PostMapping("/{productId}/reviews")
    public ResponseEntity<ProductReviewDto> createReview(@PathVariable("productId") String productId, @RequestBody CreateReviewCommand command) {
        return ResponseEntity.ok(reviewService.createReview(command));
    }

    @GetMapping("/{productId}/reviews")
    public ResponseEntity<PageResponse<ProductReviewDto>> getReviews(
        @PathVariable("productId") String productId,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId, page, size));
    }
}
