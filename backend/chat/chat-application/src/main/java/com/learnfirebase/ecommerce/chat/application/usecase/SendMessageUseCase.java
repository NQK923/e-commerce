package com.learnfirebase.ecommerce.chat.application.usecase;

import com.learnfirebase.ecommerce.chat.application.dto.SendMessageCommand;
import com.learnfirebase.ecommerce.chat.application.dto.SendMessageResult;

public interface SendMessageUseCase {
    SendMessageResult send(SendMessageCommand command);
}
