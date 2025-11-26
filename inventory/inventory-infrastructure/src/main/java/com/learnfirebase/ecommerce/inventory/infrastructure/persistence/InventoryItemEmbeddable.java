package com.learnfirebase.ecommerce.inventory.infrastructure.persistence;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemEmbeddable {
    private String productId;
    private int available;
    private int reserved;
}
