package com.learnfirebase.ecommerce.promotion.domain.model;

import java.util.UUID;

import com.learnfirebase.ecommerce.common.domain.valueobject.BaseId;

public class FlashSaleId extends BaseId<UUID> {
    public FlashSaleId(UUID value) {
        super(value);
    }
}
