package com.learnfirebase.ecommerce.identity.adapter.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.learnfirebase.ecommerce.identity.application.command.SubmitSellerApplicationCommand;
import com.learnfirebase.ecommerce.identity.application.dto.SellerApplicationDto;
import com.learnfirebase.ecommerce.identity.application.port.in.ReviewSellerApplicationUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.SellerApplicationQueryUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.SubmitSellerApplicationUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.UserQueryUseCase;
import com.learnfirebase.ecommerce.identity.application.dto.UserDto;

@ExtendWith(MockitoExtension.class)
class SellerApplicationControllerTest {

    @Mock
    private SubmitSellerApplicationUseCase submitSellerApplicationUseCase;
    @Mock
    private SellerApplicationQueryUseCase sellerApplicationQueryUseCase;
    @Mock
    private ReviewSellerApplicationUseCase reviewSellerApplicationUseCase;
    @Mock
    private UserQueryUseCase userQueryUseCase;

    private SellerApplicationController controller;

    @BeforeEach
    void setUp() {
        controller = new SellerApplicationController(
                submitSellerApplicationUseCase,
                sellerApplicationQueryUseCase,
                reviewSellerApplicationUseCase,
                userQueryUseCase
        );
    }

    @Test
    void submitRejectsUnauthenticated() {
        SellerApplicationController.SubmitSellerApplicationRequest request = new SellerApplicationController.SubmitSellerApplicationRequest();
        ResponseEntity<?> response = controller.submit(null, request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void submitRejectsDifferentUserId() {
        Authentication auth = new UsernamePasswordAuthenticationToken("user-1", null);
        SellerApplicationController.SubmitSellerApplicationRequest request = new SellerApplicationController.SubmitSellerApplicationRequest();
        request.setUserId("user-2"); // different user
        
        ResponseEntity<?> response = controller.submit(auth, request);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(submitSellerApplicationUseCase, never()).execute(any());
    }

    @Test
    void submitAcceptsSameUserId() {
        Authentication auth = new UsernamePasswordAuthenticationToken("user-1", null);
        SellerApplicationController.SubmitSellerApplicationRequest request = new SellerApplicationController.SubmitSellerApplicationRequest();
        request.setUserId("user-1");
        
        when(userQueryUseCase.getById("user-1")).thenReturn(UserDto.builder().email("user@example.com").build());
        when(submitSellerApplicationUseCase.execute(any())).thenReturn(SellerApplicationDto.builder().build());
        
        ResponseEntity<?> response = controller.submit(auth, request);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(submitSellerApplicationUseCase).execute(any());
    }

    @Test
    void submitDerivesUserIdFromAuthWhenNotProvided() {
        Authentication auth = new UsernamePasswordAuthenticationToken("user-1", null);
        SellerApplicationController.SubmitSellerApplicationRequest request = new SellerApplicationController.SubmitSellerApplicationRequest();
        // userId not set in request
        
        when(userQueryUseCase.getById("user-1")).thenReturn(UserDto.builder().email("user@example.com").build());
        when(submitSellerApplicationUseCase.execute(any())).thenReturn(SellerApplicationDto.builder().build());
        
        ResponseEntity<?> response = controller.submit(auth, request);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(submitSellerApplicationUseCase).execute(any());
    }
}
