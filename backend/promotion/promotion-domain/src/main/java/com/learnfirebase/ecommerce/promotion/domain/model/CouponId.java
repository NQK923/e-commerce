package com.learnfirebase.ecommerce.promotion.domain.model;

import com.learnfirebase.ecommerce.common.domain.valueobject.ValueObject;
import lombok.Value;

@Value
public class CouponId implements ValueObject {
    String value;
}
