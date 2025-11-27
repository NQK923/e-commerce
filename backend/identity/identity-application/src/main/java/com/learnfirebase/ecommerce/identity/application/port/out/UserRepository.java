package com.learnfirebase.ecommerce.identity.application.port.out;

import java.util.Optional;

import com.learnfirebase.ecommerce.identity.domain.model.AuthProvider;
import com.learnfirebase.ecommerce.identity.domain.model.User;
import com.learnfirebase.ecommerce.identity.domain.model.UserId;

public interface UserRepository {
    User save(User user);

    Optional<User> findByEmail(String email);

    Optional<User> findById(UserId id);

    Optional<User> findByProvider(AuthProvider provider, String providerUserId);
}
