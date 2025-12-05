package com.learnfirebase.ecommerce.identity.domain.model;

import java.util.UUID;

import com.learnfirebase.ecommerce.common.domain.valueobject.Address;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserAddress {
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    private String label; // e.g. "Home", "Office"
    private Address address;
    private boolean isDefault;
}
