package com.learnfirebase.ecommerce.identity.adapter.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;

class SecurityConfigCorsTest {
    @Test
    void corsAllowsConfiguredLocalDevOrigins() {
        SecurityConfig config = new SecurityConfig(null, null, null);
        ReflectionTestUtils.setField(
            config,
            "frontendOrigins",
            "http://localhost:3000, http://127.0.0.1:3000");

        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/products");
        request.addHeader("Origin", "http://127.0.0.1:3000");
        request.addHeader("Access-Control-Request-Method", "GET");

        CorsConfiguration cors = config.corsConfigurationSource().getCorsConfiguration(request);

        assertNotNull(cors);
        assertEquals("http://127.0.0.1:3000", cors.checkOrigin("http://127.0.0.1:3000"));
        assertEquals("http://localhost:3000", cors.checkOrigin("http://localhost:3000"));
        assertTrue(cors.getAllowCredentials());
        assertTrue(cors.getAllowedMethods().contains("OPTIONS"));
    }
}
