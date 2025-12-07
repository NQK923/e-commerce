package com.learnfirebase.ecommerce.product.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.learnfirebase.ecommerce.common.application.pagination.PageRequest;
import com.learnfirebase.ecommerce.common.application.pagination.PageResponse;
import com.learnfirebase.ecommerce.product.application.command.CreateReviewCommand;
import com.learnfirebase.ecommerce.product.application.dto.ProductReviewDto;
import com.learnfirebase.ecommerce.product.application.port.out.ProductReviewRepositoryPort;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "product_reviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewEntity {
    @Id
    private String id;
    private String productId;
    private String userId;
    private String userName;
    private Integer rating;
    private String comment;
    private Instant createdAt;
    private Instant updatedAt;
}

@Repository
interface ProductReviewJpaRepository extends JpaRepository<ProductReviewEntity, String> {
    Page<ProductReviewEntity> findByProductIdOrderByCreatedAtDesc(String productId, Pageable pageable);
    boolean existsByProductIdAndUserId(String productId, String userId);
}

@Component
@RequiredArgsConstructor
class ProductReviewRepositoryAdapter implements ProductReviewRepositoryPort {
    private final ProductReviewJpaRepository jpaRepository;

    @Override
    public ProductReviewDto save(CreateReviewCommand command) {
        ProductReviewEntity entity = ProductReviewEntity.builder()
            .id(UUID.randomUUID().toString())
            .productId(command.getProductId())
            .userId(command.getUserId())
            .userName(command.getUserName())
            .rating(command.getRating())
            .comment(command.getComment())
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
        ProductReviewEntity saved = jpaRepository.save(entity);
        return toDto(saved);
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

    private ProductReviewDto toDto(ProductReviewEntity entity) {
        return ProductReviewDto.builder()
            .id(entity.getId())
            .productId(entity.getProductId())
            .userId(entity.getUserId())
            .userName(entity.getUserName())
            .rating(entity.getRating())
            .comment(entity.getComment())
            .createdAt(entity.getCreatedAt())
            .build();
    }
}
