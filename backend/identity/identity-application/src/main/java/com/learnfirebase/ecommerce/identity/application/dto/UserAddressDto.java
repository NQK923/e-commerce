package com.learnfirebase.ecommerce.identity.application.dto;

import com.learnfirebase.ecommerce.common.domain.valueobject.Address;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserAddressDto {
    String id;
    String label;
    boolean isDefault;
    Address address;
}
