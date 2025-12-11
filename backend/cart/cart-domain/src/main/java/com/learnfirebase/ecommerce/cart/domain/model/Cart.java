package com.learnfirebase.ecommerce.cart.domain.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.learnfirebase.ecommerce.common.domain.AggregateRoot;
import com.learnfirebase.ecommerce.common.domain.valueobject.Money;

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
public class Cart extends AggregateRoot<CartId> {
    private CartId id;
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    public void addItem(CartItem item) {
        this.items.stream()
            .filter(existing -> existing.getProductId().equals(item.getProductId()) 
                && Objects.equals(existing.getVariantSku(), item.getVariantSku()))
            .findFirst()
            .ifPresentOrElse(existing -> existing.setQuantity(existing.getQuantity() + item.getQuantity()), () -> this.items.add(item));
    }

    public void updateQuantity(String productId, String variantSku, int quantity, Money price) {
        this.items.removeIf(i -> i.getProductId().equals(productId) 
            && Objects.equals(i.getVariantSku(), variantSku) && quantity <= 0);
        this.items.stream()
            .filter(i -> i.getProductId().equals(productId) && Objects.equals(i.getVariantSku(), variantSku))
            .findFirst()
            .ifPresentOrElse(
                i -> {
                    if (quantity <= 0) {
                        this.items.remove(i);
                    } else {
                        i.setQuantity(quantity);
                        if (price != null) {
                            i.setPrice(price);
                        }
                    }
                },
                () -> {
                    if (quantity > 0) {
                        this.items.add(CartItem.builder()
                            .productId(productId)
                            .variantSku(variantSku)
                            .quantity(quantity)
                            .price(price)
                            .build());
                    }
                });
    }

    public void removeItem(String productId, String variantSku) {
        this.items.removeIf(item -> item.getProductId().equals(productId) && Objects.equals(item.getVariantSku(), variantSku));
    }

    public void clear() {
        this.items.clear();
    }

    public void merge(List<CartItem> incoming) {
        incoming.forEach(this::addItem);
    }

    /**
     * Collapse duplicate items (same product + variant) into a single entry to avoid PK conflicts.
     */
    public void deduplicateItems() {
        if (items == null || items.isEmpty()) {
            return;
        }
        Map<String, CartItem> merged = new LinkedHashMap<>();
        for (CartItem item : items) {
            if (item == null || item.getProductId() == null) {
                continue;
            }
            String key = item.getProductId() + "|" + (item.getVariantSku() == null ? "" : item.getVariantSku());
            CartItem existing = merged.get(key);
            if (existing == null) {
                merged.put(key, item);
            } else {
                existing.setQuantity(existing.getQuantity() + item.getQuantity());
                if (item.getPrice() != null) {
                    existing.setPrice(item.getPrice());
                }
            }
        }
        items.clear();
        items.addAll(merged.values());
    }

    public Money total(String currency) {
        return items.stream()
            .map(i -> i.getPrice().multiply(i.getQuantity()))
            .reduce(Money.builder().amount(java.math.BigDecimal.ZERO).currency(currency).build(), Money::add);
    }
}
