package com.learnfirebase.ecommerce.identity.application.command;

import com.learnfirebase.ecommerce.common.domain.valueobject.Address;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AddAddressCommand {
    String label;
    boolean isDefault;
    Address address;
}
