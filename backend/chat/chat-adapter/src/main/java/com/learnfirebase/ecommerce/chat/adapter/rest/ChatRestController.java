package com.learnfirebase.ecommerce.chat.adapter.rest;

import com.learnfirebase.ecommerce.chat.application.dto.ChatMessageDto;
import com.learnfirebase.ecommerce.chat.application.dto.ConversationSummaryDto;
import com.learnfirebase.ecommerce.chat.application.port.in.GetConversationsUseCase;
import com.learnfirebase.ecommerce.chat.application.port.in.GetMessagesUseCase;
import com.learnfirebase.ecommerce.chat.application.port.in.MarkConversationReadUseCase;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Validated
public class ChatRestController {

    private final GetConversationsUseCase getConversationsUseCase;
    private final GetMessagesUseCase getMessagesUseCase;
    private final MarkConversationReadUseCase markConversationReadUseCase;

    @GetMapping("/conversations")
    public List<ConversationSummaryDto> listConversations(Principal principal,
                                                          @RequestHeader(value = "Authorization", required = false) String authorization) {
        String userId = requireUser(principal, authorization);
        return getConversationsUseCase.listForUser(userId);
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public List<ChatMessageDto> listMessages(@PathVariable("conversationId") String conversationId,
                                             @RequestParam(defaultValue = "50") @Min(1) @Max(200) int limit) {
        return getMessagesUseCase.listMessages(conversationId, limit);
    }

    @PostMapping("/conversations/{conversationId}/read")
    public ResponseEntity<Void> markRead(@PathVariable("conversationId") String conversationId,
                                         Principal principal,
                                         @RequestHeader(value = "Authorization", required = false) String authorization) {
        String userId = requireUser(principal, authorization);
        markConversationReadUseCase.markRead(conversationId, userId);
        return ResponseEntity.noContent().build();
    }

    private String requireUser(Principal principal, String authorization) {
        if (principal != null && principal.getName() != null && !principal.getName().isBlank()) {
            return principal.getName();
        }
        String userId = parseUserIdFromAuthorization(authorization);
        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        return userId;
    }

    private String parseUserIdFromAuthorization(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }
        String token = authorizationHeader.startsWith("Bearer ")
                ? authorizationHeader.substring("Bearer ".length())
                : authorizationHeader;
        try {
            String decoded = new String(java.util.Base64.getDecoder().decode(token), java.nio.charset.StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            return parts.length > 0 ? parts[0] : null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
