package com.learnfirebase.ecommerce.order.application.port.out;

import java.util.Optional;
import com.learnfirebase.ecommerce.promotion.domain.model.FlashSale;

public interface LoadFlashSalePort {
    Optional<FlashSale> loadFlashSale(String flashSaleId);
}
