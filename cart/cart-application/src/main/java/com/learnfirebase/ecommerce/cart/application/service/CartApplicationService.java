package com.learnfirebase.ecommerce.cart.application.service;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Collectors;

import com.learnfirebase.ecommerce.cart.application.command.AddItemCommand;
import com.learnfirebase.ecommerce.cart.application.dto.CartDto;
import com.learnfirebase.ecommerce.cart.application.port.in.ManageCartUseCase;
import com.learnfirebase.ecommerce.cart.application.port.out.CartCachePort;
import com.learnfirebase.ecommerce.cart.application.port.out.CartRepository;
import com.learnfirebase.ecommerce.cart.domain.model.Cart;
import com.learnfirebase.ecommerce.cart.domain.model.CartId;
import com.learnfirebase.ecommerce.cart.domain.model.CartItem;
import com.learnfirebase.ecommerce.common.domain.valueobject.Money;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CartApplicationService implements ManageCartUseCase {
    private final CartRepository cartRepository;
    private final CartCachePort cartCachePort;

    @Override
    public CartDto execute(AddItemCommand command) {
        Cart cart = cartRepository.findById(new CartId(command.getCartId()))
            .orElse(Cart.builder().id(new CartId(command.getCartId() != null ? command.getCartId() : UUID.randomUUID().toString())).build());
        cart.addItem(CartItem.builder()
            .productId(command.getProductId())
            .quantity(command.getQuantity())
            .price(Money.builder().amount(new BigDecimal(command.getPrice())).currency(command.getCurrency()).build())
            .build());
        Cart saved = cartRepository.save(cart);
        cartCachePort.cache(saved);
        Money total = saved.total(command.getCurrency());
        return CartDto.builder()
            .id(saved.getId().getValue())
            .currency(total.getCurrency())
            .total(total.getAmount().toPlainString())
            .items(saved.getItems().stream()
                .map(item -> CartDto.CartItemDto.builder()
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .price(item.getPrice().getAmount().toPlainString())
                    .build())
                .collect(Collectors.toList()))
            .build();
    }
}
