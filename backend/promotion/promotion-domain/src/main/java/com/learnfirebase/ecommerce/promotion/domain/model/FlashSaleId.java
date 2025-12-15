package com.learnfirebase.ecommerce.promotion.domain.model;

import java.util.UUID;

import com.learnfirebase.ecommerce.common.domain.Identifier;

public class FlashSaleId extends Identifier {
    public FlashSaleId(UUID value) {
        super(value.toString());
    }
    
    public FlashSaleId(String value) {
        super(value);
    }
}