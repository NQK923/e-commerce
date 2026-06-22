package com.learnfirebase.ecommerce.identity.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.learnfirebase.ecommerce.identity.application.port.out.PasswordHasher;

class BCryptTest {

    @Test
    void verifySeedPasswords() {
        PasswordHasher hasher = new BcryptPasswordHasher();
        
        String buyerHash = "$2a$10$z2DwVPpPiW4uU62OwtMaIe5HtnUv7u1QR7qGNbzhxO2ebTK8VF17u";
        String sellerHash = "$2a$10$vUPOVE2tAvsq41xepEHBK.nwirVl89ztbrxUkF3B3gThtk/giJLXm";

        assertThat(hasher.matches("Buyer@123", buyerHash)).isTrue();
        assertThat(hasher.matches("Seller@123", sellerHash)).isTrue();
    }
}
