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
        this.items.add(item);
    }

    public Money total(String currency) {
        return items.stream()
            .map(i -> i.getPrice().multiply(i.getQuantity()))
            .reduce(Money.builder().amount(java.math.BigDecimal.ZERO).currency(currency).build(), Money::add);
    }
}
