package com.learnfirebase.ecommerce.order.application.port.out;

import java.math.BigDecimal;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

public interface LoadProductPort {
    Map<String, ProductInfo> loadProducts(String currency, Iterable<String> productIds);

    @Value
    @Builder
    class ProductInfo {
        String id;
        BigDecimal price;
        String currency;
        String sellerId;
    }
}
