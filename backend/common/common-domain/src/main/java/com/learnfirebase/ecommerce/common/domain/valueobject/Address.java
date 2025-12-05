package com.learnfirebase.ecommerce.common.domain.valueobject;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Address {
    String fullName;
    String phoneNumber;
    String line1;
    String line2;
    String city;
    String state;
    String postalCode;
    String country;
}
