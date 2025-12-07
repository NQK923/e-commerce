package com.learnfirebase.ecommerce.product.domain.event;

import com.learnfirebase.ecommerce.common.domain.DomainEvent;
import lombok.Builder;
import lombok.Value;
import java.util.List;

@Value
@Builder
public class ProductCreatedEvent implements DomainEvent {
    String productId;
    Integer initialStock;
    List<VariantInitialStock> variants;

    @Value
    @Builder
    public static class VariantInitialStock {
        String sku;
        int quantity;
    }
}
