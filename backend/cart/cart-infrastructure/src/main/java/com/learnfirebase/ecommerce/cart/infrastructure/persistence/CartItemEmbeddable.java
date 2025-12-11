package com.learnfirebase.ecommerce.cart.infrastructure.persistence;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemEmbeddable {
    @Column(name = "product_id")
    private String productId;
    @Column(name = "variant_sku")
    private String variantSku;
    private int quantity;
    private String price;
    private String currency;
}
