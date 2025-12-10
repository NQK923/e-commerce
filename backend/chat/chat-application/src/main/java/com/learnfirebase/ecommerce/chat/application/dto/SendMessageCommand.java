package com.learnfirebase.ecommerce.chat.application.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SendMessageCommand {
    String conversationId;
    String senderId;
    String receiverId;
    String content;
}
