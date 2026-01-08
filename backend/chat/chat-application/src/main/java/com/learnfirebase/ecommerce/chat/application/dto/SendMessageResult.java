package com.learnfirebase.ecommerce.chat.application.dto;

import com.learnfirebase.ecommerce.chat.domain.model.Message;
import lombok.Value;

@Value
public class SendMessageResult {
    ChatMessageDto persistedMessage;
    boolean receiverOnline;
}
