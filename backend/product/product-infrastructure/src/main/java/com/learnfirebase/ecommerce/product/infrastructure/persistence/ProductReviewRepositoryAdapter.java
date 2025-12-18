package com.learnfirebase.ecommerce.product.infrastructure.persistence;

import com.learnfirebase.ecommerce.common.application.pagination.PageRequest;
import com.learnfirebase.ecommerce.common.application.pagination.PageResponse;
import com.learnfirebase.ecommerce.product.application.command.CreateReviewCommand;
import com.learnfirebase.ecommerce.product.application.command.RespondReviewCommand;
import com.learnfirebase.ecommerce.product.application.command.UpdateReviewCommand;
import com.learnfirebase.ecommerce.product.application.dto.ProductReviewDto;
import com.learnfirebase.ecommerce.product.application.port.out.ProductReviewRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductReviewRepositoryAdapter implements ProductReviewRepositoryPort {
    private final ProductReviewJpaRepository jpaRepository;

    @Transactional
    @Override
    public ProductReviewDto save(CreateReviewCommand command) {
        Instant now = Instant.now();
        ProductReviewEntity entity = ProductReviewEntity.builder()
            .id(UUID.randomUUID().toString())
            .productId(command.getProductId())
            .userId(command.getUserId())
            .userName(command.getUserName())
            .rating(command.getRating())
            .comment(command.getComment())
            .verifiedPurchase(command.isVerifiedPurchase())
            .abuseReportCount(0)
            .createdAt(now)
            .updatedAt(now)
            .build();
        ProductReviewEntity saved = jpaRepository.save(entity);
        return toDto(saved);
    }

    @Transactional
    @Override
    public ProductReviewDto update(UpdateReviewCommand command) {
        ProductReviewEntity entity = jpaRepository.findById(command.getReviewId())
            .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        entity.setRating(command.getRating());
        entity.setComment(command.getComment());
        entity.setUpdatedAt(Instant.now());
        return toDto(jpaRepository.save(entity));
    }

    @Transactional
    @Override
    public void delete(String reviewId) {
        if (!jpaRepository.existsById(reviewId)) {
            throw new IllegalArgumentException("Review not found");
        }
        jpaRepository.deleteById(reviewId);
    }

    @Transactional
    @Override
    public ProductReviewDto respond(RespondReviewCommand command) {
        ProductReviewEntity entity = jpaRepository.findById(command.getReviewId())
            .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        entity.setSellerResponse(command.getResponse());
        entity.setSellerRespondedAt(Instant.now());
        entity.setSellerId(command.getSellerId());
        entity.setUpdatedAt(Instant.now());
        return toDto(jpaRepository.save(entity));
    }

    @Override
    public PageResponse<ProductReviewDto> findByProductId(String productId, PageRequest pageRequest) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(pageRequest.getPage(), pageRequest.getSize());
        Page<ProductReviewEntity> page = jpaRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable);
        
        return PageResponse.<ProductReviewDto>builder()
            .content(page.getContent().stream().map(this::toDto).toList())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .page(page.getNumber())
            .size(page.getSize())
            .build();
    }

    @Override
    public boolean hasReviewed(String productId, String userId) {
        return jpaRepository.existsByProductIdAndUserId(productId, userId);
    }

    @Override
    public long countRecentReviews(String userId, Instant createdAfter) {
        return jpaRepository.countByUserIdAndCreatedAtAfter(userId, createdAfter);
    }

    @Override
    public Optional<ProductReviewDto> findById(String reviewId) {
        return jpaRepository.findById(reviewId).map(this::toDto);
    }

    @Transactional
    @Override
    public void incrementAbuseReport(String reviewId) {
        ProductReviewEntity entity = jpaRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        entity.setAbuseReportCount(entity.getAbuseReportCount() + 1);
        entity.setUpdatedAt(Instant.now());
        jpaRepository.save(entity);
    }

    private ProductReviewDto toDto(ProductReviewEntity entity) {
        return ProductReviewDto.builder()
            .id(entity.getId())
            .productId(entity.getProductId())
            .userId(entity.getUserId())
            .userName(entity.getUserName())
            .rating(entity.getRating())
            .comment(entity.getComment())
            .verifiedPurchase(entity.isVerifiedPurchase())
            .abuseReportCount(entity.getAbuseReportCount())
            .sellerResponse(entity.getSellerResponse())
            .sellerRespondedAt(entity.getSellerRespondedAt())
            .updatedAt(entity.getUpdatedAt())
            .createdAt(entity.getCreatedAt())
            .build();
    }
}
