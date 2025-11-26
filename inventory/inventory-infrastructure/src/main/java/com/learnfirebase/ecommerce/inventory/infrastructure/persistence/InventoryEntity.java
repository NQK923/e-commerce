package com.learnfirebase.ecommerce.inventory.infrastructure.persistence;

import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryEntity {
    @Id
    private String id;
    private String warehouseId;

    @ElementCollection
    @CollectionTable(name = "inventory_items", joinColumns = @JoinColumn(name = "inventory_id"))
    private List<InventoryItemEmbeddable> items;
}
