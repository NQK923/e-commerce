package com.learnfirebase.ecommerce.cart.domain.model;

import java.util.ArrayList;
import java.util.List;

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
            .filter(existing -> existing.getProductId().equals(item.getProductId()))
            .findFirst()
            .ifPresentOrElse(existing -> existing.setQuantity(existing.getQuantity() + item.getQuantity()), () -> this.items.add(item));
    }

    public void updateQuantity(String productId, int quantity, Money price) {
        this.items.removeIf(i -> i.getProductId().equals(productId) && quantity <= 0);
        this.items.stream()
            .filter(i -> i.getProductId().equals(productId))
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
                        this.items.add(CartItem.builder().productId(productId).quantity(quantity).price(price).build());
                    }
                });
    }

    public void removeItem(String productId) {
        this.items.removeIf(item -> item.getProductId().equals(productId));
    }

    public void clear() {
        this.items.clear();
    }

    public void merge(List<CartItem> incoming) {
        incoming.forEach(this::addItem);
    }

    public Money total(String currency) {
        return items.stream()
            .map(i -> i.getPrice().multiply(i.getQuantity()))
            .reduce(Money.builder().amount(java.math.BigDecimal.ZERO).currency(currency).build(), Money::add);
    }
}
