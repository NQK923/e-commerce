package com.learnfirebase.ecommerce.chat.infrastructure.messaging;

import com.learnfirebase.ecommerce.chat.application.dto.ChatMessageDto;
import com.learnfirebase.ecommerce.chat.application.port.out.MessageDeliveryPort;
import com.learnfirebase.ecommerce.chat.domain.model.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class StompMessageDeliveryAdapter implements MessageDeliveryPort {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public StompMessageDeliveryAdapter(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Override
    public void deliverToUser(String destinationUserId, Message message) {
        String destination = "/queue/chat/messages";
        ChatMessageDto dto = ChatMessageDto.builder()
                .id(message.getId().getValue())
                .conversationId(message.getConversationId().getValue())
                .senderId(message.getSenderId().getValue())
                .receiverId(message.getReceiverId().getValue())
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .status(message.getStatus())
                .build();
        simpMessagingTemplate.convertAndSendToUser(destinationUserId, destination, dto);
    }
}
