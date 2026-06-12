package com.learnfirebase.ecommerce.identity.adapter.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    @Value("${frontend.origins:${frontend.origin:http://localhost:3000},http://127.0.0.1:3000}")
    private String frontendOrigins;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/oauth2/**").permitAll()
                .requestMatchers(HttpMethod.POST,
                    "/api/auth/register",
                    "/api/auth/login",
                    "/api/auth/otp/request",
                    "/api/auth/password/forgot",
                    "/api/auth/password/reset",
                    "/api/auth/refresh-token",
                    "/api/users",
                    "/api/users/login").permitAll()
                .requestMatchers(HttpMethod.GET,
                    "/api/products",
                    "/api/products/**",
                    "/api/flash-sales",
                    "/api/inventory/product/**",
                    "/api/payments/vnpay/return").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/users/all").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/seller/applications").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/seller/applications/*/approve", "/api/seller/applications/*/reject").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/reports", "/api/reports/daily").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/reports/*/resolve", "/api/reports/*/reject").hasRole("ADMIN")
                .requestMatchers("/api/seller/promotions/**", "/api/seller/products/**").hasAnyRole("SELLER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/products/*/reviews/*/response").hasAnyRole("SELLER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/products").hasAnyRole("SELLER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/products/*").hasAnyRole("SELLER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/products/*/reviews", "/api/products/*/reviews/*/report").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/products/*/reviews/*").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/products/*/reviews/*").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/reports").authenticated()
                .requestMatchers("/api/auth/me", "/api/auth/logout", "/api/users/me/**", "/api/carts/**",
                    "/api/orders/**", "/api/promotions/apply", "/api/notifications/**", "/api/chat/**",
                    "/api/seller/applications", "/api/seller/applications/me").authenticated()
                .anyRequest().authenticated())
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                .accessDeniedHandler((request, response, accessDeniedException) ->
                    response.sendError(HttpServletResponse.SC_FORBIDDEN)))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .oauth2Login(oauth -> oauth
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(oAuth2AuthenticationSuccessHandler))
            .formLogin(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.stream(frontendOrigins.split(","))
            .map(String::trim)
            .filter(origin -> !origin.isBlank())
            .distinct()
            .toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
