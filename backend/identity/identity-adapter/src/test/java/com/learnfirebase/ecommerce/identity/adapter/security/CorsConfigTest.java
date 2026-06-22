package com.learnfirebase.ecommerce.identity.adapter.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

class CorsConfigTest {

    @Test
    void optionsRequest_withAllowedOrigin_returnsOkAndCorsHeaders() {
        SecurityConfig config = new SecurityConfig(null, null, null);
        ReflectionTestUtils.setField(config, "allowedOrigins", "http://localhost:3000,http://127.0.0.1:3000");
        ReflectionTestUtils.setField(config, "allowedMethods", "GET,POST,PUT,PATCH,DELETE,OPTIONS");
        ReflectionTestUtils.setField(config, "allowedHeaders", "Authorization,Content-Type,Accept,X-Requested-With,Cache-Control");
        ReflectionTestUtils.setField(config, "allowCredentials", true);

        UrlBasedCorsConfigurationSource source = (UrlBasedCorsConfigurationSource) config.corsConfigurationSource();
        CorsConfiguration corsConfig = source.getCorsConfiguration(new org.springframework.mock.web.MockHttpServletRequest("OPTIONS", "/api/auth/login"));

        assertThat(corsConfig).isNotNull();
        assertThat(corsConfig.getAllowedOrigins()).containsExactly("http://localhost:3000", "http://127.0.0.1:3000");
        assertThat(corsConfig.getAllowedMethods()).containsExactly("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
        assertThat(corsConfig.getAllowCredentials()).isTrue();
    }
}
