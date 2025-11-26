package com.learnfirebase.ecommerce.cart.infrastructure.persistence;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

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
    public Cart save(Cart cart) {
        CartEntity entity = toEntity(cart);
        CartEntity saved = cartJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Cart> findById(CartId id) {
        return cartJpaRepository.findById(id.getValue()).map(this::toDomain);
    }

    private CartEntity toEntity(Cart cart) {
        return CartEntity.builder()
            .id(cart.getId().getValue())
            .items(cart.getItems().stream()
                .map(item -> CartItemEmbeddable.builder()
                    .productId(item.getProductId())
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
            .items(entity.getItems().stream()
                .map(item -> CartItem.builder()
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .price(Money.builder().amount(new BigDecimal(item.getPrice())).currency(item.getCurrency()).build())
                    .build())
                .collect(Collectors.toList()))
            .build();
    }
}
