package com.learnfirebase.ecommerce.inventory.domain.model;

import java.util.ArrayList;
import java.util.List;

import com.learnfirebase.ecommerce.common.domain.AggregateRoot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Inventory extends AggregateRoot<InventoryId> {
    private InventoryId id;
    private Warehouse warehouse;
    @Builder.Default
    private List<InventoryItem> items = new ArrayList<>();

    public void adjust(String productId, int delta) {
        items.stream()
            .filter(item -> item.getProductId().equals(productId))
            .findFirst()
            .ifPresent(item -> item.setAvailable(item.getAvailable() + delta));
    }
}
