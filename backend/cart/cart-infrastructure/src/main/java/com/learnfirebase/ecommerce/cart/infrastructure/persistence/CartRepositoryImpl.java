package com.learnfirebase.ecommerce.cart.infrastructure.persistence;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.learnfirebase.ecommerce.cart.application.port.out.CartRepository;
import com.learnfirebase.ecommerce.cart.domain.model.Cart;
import com.learnfirebase.ecommerce.cart.domain.model.CartId;
import com.learnfirebase.ecommerce.cart.domain.model.CartItem;
import com.learnfirebase.ecommerce.common.domain.valueobject.Money;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CartRepositoryImpl implements CartRepository {
    private final CartJpaRepository cartJpaRepository;

    @Override
    @Transactional
    public Cart save(Cart cart) {
        CartEntity entity = toEntity(cart);
        CartEntity saved = cartJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cart> findById(CartId id) {
        return cartJpaRepository.findById(id.getValue()).map(this::toDomain);
    }

    private CartEntity toEntity(Cart cart) {
        return CartEntity.builder()
            .id(cart.getId().getValue())
            .items(cart.getItems().stream()
                .map(item -> CartItemEmbeddable.builder()
                    .productId(item.getProductId())
                    .variantSku(item.getVariantSku() == null ? "" : item.getVariantSku())
                    .quantity(item.getQuantity())
                    .price(item.getPrice().getAmount().toPlainString())
                    .currency(item.getPrice().getCurrency())
                    .build())
                .collect(Collectors.toList()))
            .build();
    }

    private Cart toDomain(CartEntity entity) {
        return Cart.builder()
            .id(new CartId(entity.getId()))
            .items(Optional.ofNullable(entity.getItems()).orElseGet(ArrayList::new).stream()
                .map(item -> CartItem.builder()
                    .productId(item.getProductId())
                    .variantSku(item.getVariantSku() == null || item.getVariantSku().isEmpty() ? null : item.getVariantSku())
                    .quantity(item.getQuantity())
                    .price(Money.builder()
                        .amount(new BigDecimal(Optional.ofNullable(item.getPrice()).orElse("0")))
                        .currency(item.getCurrency())
                        .build())
                    .build())
                .collect(Collectors.toCollection(ArrayList::new)))
            .build();
    }
}
