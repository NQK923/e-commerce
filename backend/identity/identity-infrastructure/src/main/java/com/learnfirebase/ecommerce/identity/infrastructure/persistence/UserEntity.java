package com.learnfirebase.ecommerce.identity.infrastructure.persistence;

import java.time.Instant;
import java.util.Set;

import com.learnfirebase.ecommerce.identity.domain.model.AuthProvider;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    private String id;
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;
    private String providerUserId;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles;
    @Column(name = "display_name")
    private String displayName;
    @Column(name = "avatar_url")
    private String avatarUrl;
    private Instant createdAt;
    private Instant updatedAt;
}
