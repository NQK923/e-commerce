package com.learnfirebase.ecommerce.chat.application.dto;

import com.learnfirebase.ecommerce.chat.domain.model.MessageStatus;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ChatMessageDto {
    String id;
    String conversationId;
    String senderId;
    String receiverId;
    String content;
    Instant sentAt;
    MessageStatus status;
}
