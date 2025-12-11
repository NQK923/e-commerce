package com.learnfirebase.ecommerce.cart.application.service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
            .amount(parsePrice(command.getPrice()))
            .currency(Optional.ofNullable(command.getCurrency()).orElse("USD"))
            .build();
        cart.addItem(CartItem.builder()
            .productId(command.getProductId())
            .variantSku(command.getVariantSku())
            .quantity(command.getQuantity())
            .price(money)
            .build());
        return saveAndMap(cart, money.getCurrency());
    }

    @Override
    public CartDto updateItem(UpdateItemCommand command) {
        Cart cart = getOrCreateCart(command.getCartId());
        Money money = Money.builder()
            .amount(parsePrice(command.getPrice()))
            .currency(Optional.ofNullable(command.getCurrency()).orElse("USD"))
            .build();
        cart.updateQuantity(command.getProductId(), command.getVariantSku(), command.getQuantity(), money);
        return saveAndMap(cart, money.getCurrency());
    }

    @Override
    public CartDto removeItem(RemoveItemCommand command) {
        Cart cart = getOrCreateCart(command.getCartId());
        cart.removeItem(command.getProductId(), command.getVariantSku());
        return saveAndMap(cart, null);
    }

    @Override
    public CartDto merge(MergeCartCommand command) {
        Cart cart = getOrCreateCart(command.getCartId());
        List<CartItem> items = command.getItems() != null
            ? command.getItems().stream()
                .map(item -> CartItem.builder()
                    .productId(item.getProductId())
                    .variantSku(item.getVariantSku())
                    .quantity(item.getQuantity())
                    .price(Money.builder()
                        .amount(parsePrice(item.getPrice()))
                        .currency(Optional.ofNullable(item.getCurrency()).orElse("USD"))
                        .build())
                    .build())
                .collect(Collectors.toList())
            : Collections.emptyList();
        cart.merge(items);
        String currency = items.stream().findFirst().map(i -> i.getPrice().getCurrency()).orElse(null);
        return saveAndMap(cart, currency);
    }

    private BigDecimal parsePrice(String price) {
        if (price == null || price.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(price);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
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
        return saveAndMap(cart, resolveCurrency(cart, null));
    }

    private Cart getOrCreateCart(String cartId) {
        String resolvedId = cartId != null ? cartId : UUID.randomUUID().toString();
        return cartRepository.findById(new CartId(resolvedId))
            .orElse(Cart.builder().id(new CartId(resolvedId)).items(new java.util.ArrayList<>()).build());
    }

    private CartDto saveAndMap(Cart cart, String currencyHint) {
        cart.deduplicateItems();
        String currency = resolveCurrency(cart, currencyHint);
        normalizeCurrency(cart, currency);
        Cart saved = cartRepository.save(cart);
        cartCachePort.cache(saved);
        return mapToDto(saved, currency);
    }

    private String resolveCurrency(Cart cart, String currencyHint) {
        return Optional.ofNullable(currencyHint)
            .orElseGet(() -> cart.getItems().stream()
                .map(item -> Optional.ofNullable(item.getPrice()).map(Money::getCurrency).orElse(null))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("USD"));
    }

    private CartDto mapToDto(Cart cart, String currency) {
        Money total = cart.total(Optional.ofNullable(currency).orElse("USD"));
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
                    .variantSku(item.getVariantSku())
                    .quantity(item.getQuantity())
                    .price(item.getPrice().getAmount().toPlainString())
                    .currency(item.getPrice().getCurrency())
                    .build())
                .collect(Collectors.toList()))
            .build();
    }

    /**
     * Ensure all cart items share the same currency to avoid Money.add mismatches.
     */
    private void normalizeCurrency(Cart cart, String currency) {
        if (currency == null) {
            return;
        }
        cart.getItems().forEach(item -> {
            if (item.getPrice() != null && (item.getPrice().getCurrency() == null
                || !currency.equalsIgnoreCase(item.getPrice().getCurrency()))) {
                item.setPrice(Money.builder()
                    .amount(item.getPrice().getAmount())
                    .currency(currency)
                    .build());
            }
        });
    }
}
