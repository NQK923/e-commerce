package com.learnfirebase.ecommerce.cart.infrastructure.persistence;

import java.util.List;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "carts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartEntity {
    @Id
    private String id;

    @ElementCollection
    private List<CartItemEmbeddable> items;
}
