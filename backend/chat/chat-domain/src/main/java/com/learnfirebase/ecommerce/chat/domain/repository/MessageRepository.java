package com.learnfirebase.ecommerce.chat.domain.repository;

import com.learnfirebase.ecommerce.chat.domain.model.ConversationId;
import com.learnfirebase.ecommerce.chat.domain.model.Message;
import com.learnfirebase.ecommerce.chat.domain.model.MessageId;
import java.util.List;
import java.util.Optional;

public interface MessageRepository {
    Message save(Message message);

    Optional<Message> findById(MessageId id);

    List<Message> findRecentByConversation(ConversationId conversationId, int limit);
}
