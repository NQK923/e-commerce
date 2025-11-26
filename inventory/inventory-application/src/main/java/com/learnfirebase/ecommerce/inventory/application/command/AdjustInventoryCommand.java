package com.learnfirebase.ecommerce.inventory.application.command;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AdjustInventoryCommand {
    String inventoryId;
    String productId;
    int delta;
}
