package com.learnfirebase.ecommerce.cart.application.service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.learnfirebase.ecommerce.cart.application.command.AddItemCommand;
import com.learnfirebase.ecommerce.cart.application.command.ClearCartCommand;
import com.learnfirebase.ecommerce.cart.application.command.MergeCartCommand;
import com.learnfirebase.ecommerce.cart.application.command.RemoveItemCommand;
import com.learnfirebase.ecommerce.cart.application.command.UpdateItemCommand;
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
    public CartDto addItem(AddItemCommand command) {
        Cart cart = getOrCreateCart(command.getCartId());
        Money money = Money.builder()
            .amount(new BigDecimal(Optional.ofNullable(command.getPrice()).orElse("0")))
            .currency(Optional.ofNullable(command.getCurrency()).orElse("USD"))
            .build();
        cart.addItem(CartItem.builder()
            .productId(command.getProductId())
            .quantity(command.getQuantity())
            .price(money)
            .build());
        return saveAndMap(cart, money.getCurrency());
    }

    @Override
    public CartDto updateItem(UpdateItemCommand command) {
        Cart cart = getOrCreateCart(command.getCartId());
        Money money = Money.builder()
            .amount(new BigDecimal(Optional.ofNullable(command.getPrice()).orElse("0")))
            .currency(Optional.ofNullable(command.getCurrency()).orElse("USD"))
            .build();
        cart.updateQuantity(command.getProductId(), command.getQuantity(), money);
        return saveAndMap(cart, money.getCurrency());
    }

    @Override
    public CartDto removeItem(RemoveItemCommand command) {
        Cart cart = getOrCreateCart(command.getCartId());
        cart.removeItem(command.getProductId());
        return saveAndMap(cart, null);
    }

    @Override
    public CartDto merge(MergeCartCommand command) {
        Cart cart = getOrCreateCart(command.getCartId());
        List<CartItem> items = command.getItems() != null
            ? command.getItems().stream()
                .map(item -> CartItem.builder()
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .price(Money.builder()
                        .amount(new BigDecimal(Optional.ofNullable(item.getPrice()).orElse("0")))
                        .currency(Optional.ofNullable(item.getCurrency()).orElse("USD"))
                        .build())
                    .build())
                .collect(Collectors.toList())
            : Collections.emptyList();
        cart.merge(items);
        String currency = items.stream().findFirst().map(i -> i.getPrice().getCurrency()).orElse(null);
        return saveAndMap(cart, currency);
    }

    @Override
    public CartDto clear(ClearCartCommand command) {
        Cart cart = getOrCreateCart(command.getCartId());
        cart.clear();
        return saveAndMap(cart, null);
    }

    @Override
    public CartDto get(String cartId) {
        Cart cart = getOrCreateCart(cartId);
        String currency = cart.getItems().stream().findFirst().map(item -> item.getPrice().getCurrency()).orElse("USD");
        return mapToDto(cart, currency);
    }

    private Cart getOrCreateCart(String cartId) {
        String resolvedId = cartId != null ? cartId : UUID.randomUUID().toString();
        return cartRepository.findById(new CartId(resolvedId))
            .orElse(Cart.builder().id(new CartId(resolvedId)).items(new java.util.ArrayList<>()).build());
    }

    private CartDto saveAndMap(Cart cart, String currencyHint) {
        Cart saved = cartRepository.save(cart);
        cartCachePort.cache(saved);
        String currency = currencyHint != null ? currencyHint
            : saved.getItems().stream().findFirst().map(i -> i.getPrice().getCurrency()).orElse("USD");
        return mapToDto(saved, currency);
    }

    private CartDto mapToDto(Cart cart, String currency) {
        Money total = cart.total(currency);
        return CartDto.builder()
            .id(cart.getId().getValue())
            .currency(total.getCurrency())
            .total(total.getAmount().toPlainString())
            .subtotal(total.getAmount().toPlainString())
            .discountTotal("0")
            .shippingEstimate("0")
            .items(cart.getItems().stream()
                .map(item -> CartDto.CartItemDto.builder()
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .price(item.getPrice().getAmount().toPlainString())
                    .currency(item.getPrice().getCurrency())
                    .build())
                .collect(Collectors.toList()))
            .build();
    }
}
