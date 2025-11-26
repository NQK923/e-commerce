package com.learnfirebase.ecommerce.identity.infrastructure.persistence;

import java.time.Instant;
import java.util.Set;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
    @ElementCollection
    private Set<String> roles;
    private Instant createdAt;
    private Instant updatedAt;
}
