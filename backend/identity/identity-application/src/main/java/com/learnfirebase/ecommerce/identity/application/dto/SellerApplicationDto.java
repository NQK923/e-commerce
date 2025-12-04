package com.learnfirebase.ecommerce.identity.application.dto;

import java.time.Instant;

import com.learnfirebase.ecommerce.identity.domain.model.SellerApplicationStatus;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SellerApplicationDto {
    String id;
    String userId;
    String storeName;
    String contactEmail;
    String phone;
    String category;
    String description;
    String avatarUrl;
    String coverUrl;
    SellerApplicationStatus status;
    boolean acceptedTerms;
    Instant createdAt;
    Instant updatedAt;
}
