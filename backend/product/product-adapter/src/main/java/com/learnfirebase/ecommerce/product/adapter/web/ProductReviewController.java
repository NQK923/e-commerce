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

    @PostMapping("/{id}/reviews")
    public ResponseEntity<ProductReviewDto> createReview(
            @PathVariable String id,
            @RequestBody CreateReviewCommand command) {
        // Ensure the productId in command matches path
        command.setProductId(id);
        return ResponseEntity.ok(reviewService.createReview(command));
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<PageResponse<ProductReviewDto>> getReviews(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.getProductReviews(id, page, size));
    }
}
