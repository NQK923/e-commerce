package com.learnfirebase.ecommerce.inventory.application.dto;

import java.util.List;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
public class InventoryDto {
    String id;
    String warehouseId;
    @Singular
    List<ItemDto> items;

    @Value
    @Builder
    public static class ItemDto {
        String productId;
        int available;
        int reserved;
    }
}
