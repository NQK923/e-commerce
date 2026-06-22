package com.learnfirebase.ecommerce.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import com.learnfirebase.ecommerce.bootstrap.EcommerceApplication;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.learnfirebase.ecommerce.cart.adapter.web.CartController;
import com.learnfirebase.ecommerce.cart.application.port.in.ManageCartUseCase;
import com.learnfirebase.ecommerce.chat.adapter.rest.ChatRestController;
import com.learnfirebase.ecommerce.chat.application.dto.ChatMessageDto;
import com.learnfirebase.ecommerce.chat.application.port.in.GetConversationsUseCase;
import com.learnfirebase.ecommerce.chat.application.port.in.GetMessagesUseCase;
import com.learnfirebase.ecommerce.chat.application.port.in.MarkConversationReadUseCase;
import com.learnfirebase.ecommerce.common.domain.AccessDeniedDomainException;
import com.learnfirebase.ecommerce.common.domain.ResourceNotFoundDomainException;
import com.learnfirebase.ecommerce.identity.adapter.security.CustomOAuth2UserService;
import com.learnfirebase.ecommerce.identity.adapter.security.JwtAuthenticationFilter;
import com.learnfirebase.ecommerce.identity.adapter.security.OAuth2AuthenticationSuccessHandler;
import com.learnfirebase.ecommerce.identity.adapter.security.SecurityConfig;
import com.learnfirebase.ecommerce.identity.application.port.out.TokenProvider;
import com.learnfirebase.ecommerce.identity.application.port.out.UserRepository;
import com.learnfirebase.ecommerce.identity.domain.model.Role;
import com.learnfirebase.ecommerce.identity.domain.model.User;
import com.learnfirebase.ecommerce.identity.domain.model.UserId;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = {CartController.class, ChatRestController.class})
@ContextConfiguration(classes = EcommerceApplication.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, CustomOAuth2UserService.class, OAuth2AuthenticationSuccessHandler.class})
class ProtectedControllersSecurityTest {

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

    // Cart dependencies
    @MockBean
    private ManageCartUseCase manageCartUseCase;

    // Chat dependencies
    @MockBean
    private GetConversationsUseCase getConversationsUseCase;

    @MockBean
    private GetMessagesUseCase getMessagesUseCase;

    @MockBean
    private MarkConversationReadUseCase markConversationReadUseCase;

    private void setupMockUser(String token, String userId, Role role) {
        when(tokenProvider.validateAndGetUserId(token)).thenReturn(userId);
        when(userRepository.findById(new UserId(userId))).thenReturn(Optional.of(
            User.builder().id(new UserId(userId)).roles(Set.of(role)).build()
        ));
    }

    @Test
    void cartEndpointsDenyAnonymous() throws Exception {
        mockMvc.perform(get("/api/carts")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/carts/items")).andExpect(status().isUnauthorized());
    }

    @Test
    void chatEndpointsDenyAnonymous() throws Exception {
        mockMvc.perform(get("/api/chat/conversations")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/chat/conversations/conv-1/messages")).andExpect(status().isUnauthorized());
    }

    @Test
    void cartGetSucceedsWithValidToken() throws Exception {
        setupMockUser("valid-token", "user-123", Role.CUSTOMER);
        when(manageCartUseCase.get("user-123")).thenReturn(null);

        mockMvc.perform(get("/api/carts").header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk());
    }

    @Test
    void chatListMessagesSucceedsForParticipant() throws Exception {
        setupMockUser("valid-token", "user-123", Role.CUSTOMER);
        when(getMessagesUseCase.listMessages(eq("conv-1"), eq("user-123"), eq(50)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/chat/conversations/conv-1/messages").header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk());
    }

    @Test
    void chatListMessagesReturnsForbiddenForNonParticipant() throws Exception {
        setupMockUser("valid-token", "user-123", Role.CUSTOMER);
        when(getMessagesUseCase.listMessages(eq("conv-1"), eq("user-123"), eq(50)))
                .thenThrow(new AccessDeniedDomainException("User not participant"));

        mockMvc.perform(get("/api/chat/conversations/conv-1/messages").header("Authorization", "Bearer valid-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void chatListMessagesReturnsNotFoundForNonExistentConversation() throws Exception {
        setupMockUser("valid-token", "user-123", Role.CUSTOMER);
        when(getMessagesUseCase.listMessages(eq("conv-1"), eq("user-123"), eq(50)))
                .thenThrow(new ResourceNotFoundDomainException("Conversation not found"));

        mockMvc.perform(get("/api/chat/conversations/conv-1/messages").header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNotFound());
    }
}
