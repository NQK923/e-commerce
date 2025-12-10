package com.learnfirebase.ecommerce.chat.application.port.in;

public interface MarkConversationReadUseCase {
    void markRead(String conversationId, String userId);
}
