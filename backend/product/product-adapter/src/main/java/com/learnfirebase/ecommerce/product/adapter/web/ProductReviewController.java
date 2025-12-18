package com.learnfirebase.ecommerce.product.adapter.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learnfirebase.ecommerce.common.application.pagination.PageResponse;
import com.learnfirebase.ecommerce.product.application.command.CreateReviewCommand;
import com.learnfirebase.ecommerce.product.application.command.DeleteReviewCommand;
import com.learnfirebase.ecommerce.product.application.command.ReportReviewCommand;
import com.learnfirebase.ecommerce.product.application.command.RespondReviewCommand;
import com.learnfirebase.ecommerce.product.application.command.UpdateReviewCommand;
import com.learnfirebase.ecommerce.product.application.dto.ProductReviewDto;
import com.learnfirebase.ecommerce.product.application.exception.ReviewRateLimitException;
import com.learnfirebase.ecommerce.product.application.service.ProductReviewService;
import com.learnfirebase.ecommerce.product.domain.model.ReportReason;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductReviewController {
    private final ProductReviewService reviewService;

    @PostMapping("/{productId}/reviews")
    public ResponseEntity<ProductReviewDto> createReview(
        @PathVariable("productId") String productId,
        @RequestBody CreateReviewCommand command,
        @AuthenticationPrincipal Jwt jwt) {
        command.setProductId(productId);
        if (jwt != null) {
            command.setUserId(jwt.getSubject());
            if (command.getUserName() == null || command.getUserName().isBlank()) {
                command.setUserName(jwt.getClaimAsString("name"));
            }
        }
        return ResponseEntity.ok(reviewService.createReview(command));
    }

    @PutMapping("/{productId}/reviews/{reviewId}")
    public ResponseEntity<ProductReviewDto> updateReview(
        @PathVariable("productId") String productId,
        @PathVariable("reviewId") String reviewId,
        @RequestBody UpdateReviewCommand command,
        @AuthenticationPrincipal Jwt jwt) {
        command.setProductId(productId);
        command.setReviewId(reviewId);
        if (jwt != null) {
            command.setUserId(jwt.getSubject());
        }
        return ResponseEntity.ok(reviewService.updateReview(command));
    }

    @DeleteMapping("/{productId}/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
        @PathVariable("productId") String productId,
        @PathVariable("reviewId") String reviewId,
        @RequestBody(required = false) DeleteReviewCommand command,
        @AuthenticationPrincipal Jwt jwt) {
        DeleteReviewCommand payload = command != null ? command : new DeleteReviewCommand();
        payload.setProductId(productId);
        payload.setReviewId(reviewId);
        if (jwt != null) {
            payload.setUserId(jwt.getSubject());
        }
        reviewService.deleteReview(payload);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{productId}/reviews/{reviewId}/report")
    public ResponseEntity<Void> reportReview(
        @PathVariable("productId") String productId,
        @PathVariable("reviewId") String reviewId,
        @RequestBody ReportReviewCommand command,
        @AuthenticationPrincipal Jwt jwt) {
        command.setProductId(productId);
        command.setReviewId(reviewId);
        if (jwt != null) {
            command.setReporterUserId(jwt.getSubject());
        }
        if (command.getReason() == null) {
            command.setReason(ReportReason.OTHER);
        }
        reviewService.reportAbuse(command);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PostMapping("/{productId}/reviews/{reviewId}/response")
    public ResponseEntity<ProductReviewDto> respondToReview(
        @PathVariable("productId") String productId,
        @PathVariable("reviewId") String reviewId,
        @RequestBody RespondReviewCommand command,
        @AuthenticationPrincipal Jwt jwt) {
        command.setProductId(productId);
        command.setReviewId(reviewId);
        if (jwt != null) {
            command.setSellerId(jwt.getSubject());
        }
        return ResponseEntity.ok(reviewService.respondToReview(command));
    }

    @GetMapping("/{productId}/reviews")
    public ResponseEntity<PageResponse<ProductReviewDto>> getReviews(
        @PathVariable("productId") String productId,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId, page, size));
    }

    @ExceptionHandler(ReviewRateLimitException.class)
    public ResponseEntity<String> handleRateLimit(ReviewRateLimitException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ex.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<String> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
