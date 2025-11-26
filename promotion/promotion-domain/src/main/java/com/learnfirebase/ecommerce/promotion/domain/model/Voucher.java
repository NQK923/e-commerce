package com.learnfirebase.ecommerce.promotion.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Voucher {
    String code;
    double discountPercentage;
    boolean active;
}
