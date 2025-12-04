package com.learnfirebase.ecommerce.identity.domain.model;

import java.time.Instant;

import com.learnfirebase.ecommerce.common.domain.AggregateRoot;
import com.learnfirebase.ecommerce.common.domain.valueobject.Email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
public class SellerApplication extends AggregateRoot<SellerApplicationId> {
    private SellerApplicationId id;
    private UserId userId;
    private String storeName;
    private Email contactEmail;
    private String phone;
    private String category;
    private String description;
    @Builder.Default
    private SellerApplicationStatus status = SellerApplicationStatus.PENDING;
    @Builder.Default
    private boolean acceptedTerms = false;
    private Instant createdAt;
    private Instant updatedAt;

    public boolean isPending() {
        return SellerApplicationStatus.PENDING.equals(status);
    }

    public boolean isApproved() {
        return SellerApplicationStatus.APPROVED.equals(status);
    }
}
