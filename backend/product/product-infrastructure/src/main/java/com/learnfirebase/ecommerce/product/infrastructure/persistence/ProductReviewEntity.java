package com.learnfirebase.ecommerce.product.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.learnfirebase.ecommerce.common.application.pagination.PageRequest;
import com.learnfirebase.ecommerce.common.application.pagination.PageResponse;
import com.learnfirebase.ecommerce.product.application.command.CreateReviewCommand;
import com.learnfirebase.ecommerce.product.application.command.RespondReviewCommand;
import com.learnfirebase.ecommerce.product.application.command.UpdateReviewCommand;
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
import org.springframework.transaction.annotation.Transactional;

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
    private boolean verifiedPurchase;
    private int abuseReportCount;
    private String sellerResponse;
    private Instant sellerRespondedAt;
    private String sellerId;
    private Instant createdAt;
    private Instant updatedAt;
}

