package com.learnfirebase.ecommerce.identity.adapter.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.learnfirebase.ecommerce.identity.application.port.out.TokenProvider;
import com.learnfirebase.ecommerce.identity.application.port.out.UserRepository;
import com.learnfirebase.ecommerce.identity.domain.model.Role;
import com.learnfirebase.ecommerce.identity.domain.model.User;
import com.learnfirebase.ecommerce.identity.domain.model.UserId;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = {})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, CustomOAuth2UserService.class, OAuth2AuthenticationSuccessHandler.class})
class SecurityRoutesTest {

    @SpringBootApplication
    static class TestApp {}

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TokenProvider tokenProvider;

    @MockBean
    private UserRepository userRepository;
    
    @MockBean
    private CustomOAuth2UserService customOAuth2UserService;
    
    @MockBean
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @Test
    void publicRoutes_ShouldAllowAnonymous() throws Exception {
        mockMvc.perform(get("/api/products")).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/flash-sales")).andExpect(status().isNotFound());
        mockMvc.perform(post("/api/auth/login")).andExpect(status().isNotFound());
        mockMvc.perform(post("/api/auth/register")).andExpect(status().isNotFound());
    }

    @Test
    void protectedRoutes_ShouldDenyAnonymous() throws Exception {
        mockMvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/carts/items")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/orders")).andExpect(status().isUnauthorized());
    }

    private void setupMockUser(String token, String userId, Role role) {
        when(tokenProvider.validateAndGetUserId(token)).thenReturn(userId);
        when(userRepository.findById(new UserId(userId))).thenReturn(Optional.of(
            User.builder().id(new UserId(userId)).roles(Set.of(role)).build()
        ));
    }

    @Test
    void customer_ShouldAccessCustomerRoutes_ButNotAdminOrSeller() throws Exception {
        setupMockUser("customer-token", "cust-1", Role.CUSTOMER);
        
        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer customer-token")).andExpect(status().isNotFound());
        mockMvc.perform(post("/api/carts/items").header("Authorization", "Bearer customer-token")).andExpect(status().isNotFound());
        
        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer customer-token")).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/seller/products").header("Authorization", "Bearer customer-token")).andExpect(status().isForbidden());
    }

    @Test
    void seller_ShouldAccessSellerRoutes_ButNotAdmin() throws Exception {
        setupMockUser("seller-token", "seller-1", Role.SELLER);

        mockMvc.perform(post("/api/seller/products").header("Authorization", "Bearer seller-token")).andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/products/123").header("Authorization", "Bearer seller-token")).andExpect(status().isNotFound());
        
        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer seller-token")).andExpect(status().isForbidden());
    }

    @Test
    void admin_ShouldAccessAdminAndSellerRoutes() throws Exception {
        setupMockUser("admin-token", "admin-1", Role.ADMIN);

        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer admin-token")).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/reports").header("Authorization", "Bearer admin-token")).andExpect(status().isNotFound());
        mockMvc.perform(post("/api/seller/products").header("Authorization", "Bearer admin-token")).andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/products/123").header("Authorization", "Bearer admin-token")).andExpect(status().isNotFound());
    }
}
