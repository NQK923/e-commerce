package com.learnfirebase.ecommerce.chat.application.dto;

import lombok.Value;

@Value
public class SendMessageResult {
    ChatMessageDto persistedMessage;
    boolean receiverOnline;
}
