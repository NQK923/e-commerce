package com.learnfirebase.ecommerce.chat.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypingEvent {
    private String conversationId;
    private String senderId;
    private String receiverId;
    private boolean isTyping;
}
