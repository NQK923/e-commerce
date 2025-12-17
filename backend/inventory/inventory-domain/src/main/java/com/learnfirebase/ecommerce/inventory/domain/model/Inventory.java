package com.learnfirebase.ecommerce.inventory.domain.model;

import java.util.ArrayList;
import java.util.List;

import com.learnfirebase.ecommerce.common.domain.AggregateRoot;
import com.learnfirebase.ecommerce.inventory.domain.exception.InventoryDomainException;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
public class Inventory extends AggregateRoot<InventoryId> {
    private InventoryId id;
    private Warehouse warehouse;
    @Builder.Default
    private List<InventoryItem> items = new ArrayList<>();

    public void adjust(String productId, int delta) {
        var itemOptional = items.stream()
            .filter(item -> item.getProductId().equals(productId))
            .findFirst();

        if (itemOptional.isPresent()) {
            InventoryItem item = itemOptional.get();
            int next = item.getAvailable() + delta;
            if (next < 0) {
                throw new InventoryDomainException("Insufficient stock for product " + productId);
            }
            item.setAvailable(next);
        } else {
            items.add(InventoryItem.builder()
                .productId(productId)
                .available(delta)
                .reserved(0)
                .build());
        }
    }
}
