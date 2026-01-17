package com.learnfirebase.ecommerce.chat.adapter.websocket;

import com.learnfirebase.ecommerce.chat.application.dto.SendMessageCommand;
import com.learnfirebase.ecommerce.chat.application.dto.SendMessageResult;
import com.learnfirebase.ecommerce.chat.application.usecase.SendMessageUseCase;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SendMessageUseCase sendMessageUseCase;
    private final org.springframework.messaging.simp.SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/chat.send")
    @SendToUser("/queue/chat/ack")
    public SendMessageResult send(@Payload ChatMessageRequest request, Principal principal) {
        String senderId = principal.getName();
        SendMessageCommand command = SendMessageCommand.builder()
                .conversationId(request.getConversationId())
                .senderId(senderId)
                .receiverId(request.getReceiverId())
                .content(request.getContent())
                .build();
        return sendMessageUseCase.send(command);
    }

    @MessageMapping("/chat.typing")
    public void typing(@Payload com.learnfirebase.ecommerce.chat.application.dto.TypingEvent event,
            Principal principal) {
        // Ensure the sender is who they say they are (optional security check)
        String senderId = principal.getName();
        event.setSenderId(senderId);

        // Forward to the receiver
        simpMessagingTemplate.convertAndSendToUser(
                java.util.Objects.requireNonNull(event.getReceiverId()),
                "/queue/chat/typing",
                event);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessageRequest {
        private String conversationId;
        private String receiverId;
        private String content;
    }
}
