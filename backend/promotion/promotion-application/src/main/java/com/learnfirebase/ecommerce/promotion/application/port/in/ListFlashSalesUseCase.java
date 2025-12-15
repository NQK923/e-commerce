package com.learnfirebase.ecommerce.promotion.application.port.in;

import java.util.List;

import com.learnfirebase.ecommerce.promotion.domain.model.FlashSale;

public interface ListFlashSalesUseCase {
    List<FlashSale> listActiveFlashSales();
}
