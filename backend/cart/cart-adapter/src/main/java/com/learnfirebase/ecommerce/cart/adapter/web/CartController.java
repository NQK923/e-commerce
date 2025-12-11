package com.learnfirebase.ecommerce.cart.adapter.web;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learnfirebase.ecommerce.cart.application.command.AddItemCommand;
import com.learnfirebase.ecommerce.cart.application.command.ClearCartCommand;
import com.learnfirebase.ecommerce.cart.application.command.MergeCartCommand;
import com.learnfirebase.ecommerce.cart.application.command.RemoveItemCommand;
import com.learnfirebase.ecommerce.cart.application.command.UpdateItemCommand;
import com.learnfirebase.ecommerce.cart.application.dto.CartDto;
import com.learnfirebase.ecommerce.cart.application.port.in.ManageCartUseCase;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {
    private final ManageCartUseCase manageCartUseCase;

    @GetMapping
    public ResponseEntity<CartDto> getCart(@RequestHeader(name = "Authorization", required = false) String authorization) {
        String cartId = extractUserId(authorization);
        return ResponseEntity.ok(manageCartUseCase.get(cartId));
    }

    @PostMapping("/items")
    public ResponseEntity<CartDto> addItem(@RequestBody AddItemCommand command,
        @RequestHeader(name = "Authorization", required = false) String authorization) {
        String cartId = command.getCartId() != null ? command.getCartId() : extractUserId(authorization);
        AddItemCommand effective = AddItemCommand.builder()
            .cartId(cartId)
            .productId(command.getProductId())
            .variantSku(command.getVariantSku())
            .quantity(command.getQuantity())
            .price(command.getPrice())
            .currency(command.getCurrency())
            .build();
        return ResponseEntity.ok(manageCartUseCase.addItem(effective));
    }

    @PatchMapping("/items/{productId}")
    public ResponseEntity<CartDto> updateItem(@PathVariable("productId") String productId, @RequestBody UpdateItemCommand command,
        @RequestHeader(name = "Authorization", required = false) String authorization) {
        String cartId = command.getCartId() != null ? command.getCartId() : extractUserId(authorization);
        UpdateItemCommand effective = UpdateItemCommand.builder()
            .cartId(cartId)
            .productId(productId)
            .variantSku(command.getVariantSku())
            .quantity(command.getQuantity())
            .price(command.getPrice())
            .currency(command.getCurrency())
            .build();
        return ResponseEntity.ok(manageCartUseCase.updateItem(effective));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartDto> removeItem(@PathVariable("productId") String productId,
        @RequestParam(required = false) String variantSku,
        @RequestHeader(name = "Authorization", required = false) String authorization) {
        String cartId = extractUserId(authorization);
        RemoveItemCommand command = RemoveItemCommand.builder()
            .cartId(cartId)
            .productId(productId)
            .variantSku(variantSku)
            .build();
        return ResponseEntity.ok(manageCartUseCase.removeItem(command));
    }

    @PostMapping("/merge")
    public ResponseEntity<CartDto> merge(@RequestBody MergeCartCommand command,
        @RequestHeader(name = "Authorization", required = false) String authorization) {
        String cartId = command.getCartId() != null ? command.getCartId() : extractUserId(authorization);
        MergeCartCommand effective = MergeCartCommand.builder()
            .cartId(cartId)
            .items(command.getItems())
            .build();
        return ResponseEntity.ok(manageCartUseCase.merge(effective));
    }

    @PostMapping("/clear")
    public ResponseEntity<CartDto> clear(@RequestBody(required = false) ClearCartCommand command,
        @RequestHeader(name = "Authorization", required = false) String authorization) {
        String cartId = command != null && command.getCartId() != null ? command.getCartId() : extractUserId(authorization);
        ClearCartCommand effective = ClearCartCommand.builder()
            .cartId(cartId)
            .build();
        return ResponseEntity.ok(manageCartUseCase.clear(effective));
    }

    private String extractUserId(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        try {
            String token = authorization.substring(7);
            String decoded = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            if (parts.length >= 1) {
                return parts[0];
            }
        } catch (IllegalArgumentException ignored) {
            // invalid token
        }
        return null;
    }
}
