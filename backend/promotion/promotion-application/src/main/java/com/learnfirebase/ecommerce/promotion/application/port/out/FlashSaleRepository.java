package com.learnfirebase.ecommerce.promotion.application.port.out;

import java.util.Optional;

import com.learnfirebase.ecommerce.promotion.domain.model.FlashSale;
import com.learnfirebase.ecommerce.promotion.domain.model.FlashSaleId;

public interface FlashSaleRepository {
    FlashSale save(FlashSale flashSale);
    Optional<FlashSale> findById(FlashSaleId id);
    Optional<FlashSale> findActiveByProductId(String productId);
    java.util.List<FlashSale> findAllActive();
}
