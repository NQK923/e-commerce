package com.learnfirebase.ecommerce.cart.adapter.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learnfirebase.ecommerce.cart.application.command.AddItemCommand;
import com.learnfirebase.ecommerce.cart.application.dto.CartDto;
import com.learnfirebase.ecommerce.cart.application.port.in.ManageCartUseCase;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {
    private final ManageCartUseCase manageCartUseCase;

    @PostMapping("/items")
    public ResponseEntity<CartDto> addItem(@RequestBody AddItemCommand command) {
        return ResponseEntity.ok(manageCartUseCase.execute(command));
    }
}
