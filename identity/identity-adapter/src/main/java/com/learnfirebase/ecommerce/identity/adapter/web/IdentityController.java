package com.learnfirebase.ecommerce.identity.adapter.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learnfirebase.ecommerce.identity.application.command.LoginCommand;
import com.learnfirebase.ecommerce.identity.application.command.RegisterUserCommand;
import com.learnfirebase.ecommerce.identity.application.dto.AuthTokenDto;
import com.learnfirebase.ecommerce.identity.application.dto.UserDto;
import com.learnfirebase.ecommerce.identity.application.port.in.AuthenticateUserUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.RegisterUserUseCase;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class IdentityController {
    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;

    @PostMapping
    public ResponseEntity<UserDto> register(@RequestBody RegisterUserCommand command) {
        return ResponseEntity.ok(registerUserUseCase.execute(command));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthTokenDto> login(@RequestBody LoginCommand command) {
        return ResponseEntity.ok(authenticateUserUseCase.execute(command));
    }
}
