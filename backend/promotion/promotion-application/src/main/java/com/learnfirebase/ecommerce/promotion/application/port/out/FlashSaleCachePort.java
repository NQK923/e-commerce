package com.learnfirebase.ecommerce.promotion.application.port.out;

import com.learnfirebase.ecommerce.promotion.domain.model.FlashSaleId;

public interface FlashSaleCachePort {
    void setStock(FlashSaleId id, int quantity);
    boolean decrementStock(FlashSaleId id, int quantity);
    Integer getStock(FlashSaleId id);
}
