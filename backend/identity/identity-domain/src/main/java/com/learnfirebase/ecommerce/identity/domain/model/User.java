package com.learnfirebase.ecommerce.identity.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.learnfirebase.ecommerce.common.domain.AggregateRoot;
import com.learnfirebase.ecommerce.common.domain.valueobject.Email;

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
public class User extends AggregateRoot<UserId> {
    private UserId id;
    private Email email;
    private HashedPassword password;
    @Builder.Default
    private AuthProvider authProvider = AuthProvider.LOCAL;
    private String providerUserId;
    @Builder.Default
    private Set<Role> roles = EnumSet.of(Role.CUSTOMER);
    @Builder.Default
    private Set<Permission> permissions = EnumSet.noneOf(Permission.class);
    @Builder.Default
    private List<UserAddress> addresses = new ArrayList<>();
    private String displayName;
    private String avatarUrl;
    private Instant createdAt;
    private Instant updatedAt;

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void addPermission(Permission permission) {
        this.permissions.add(permission);
    }
}
