package com.learnfirebase.ecommerce.chat.domain.repository;

import com.learnfirebase.ecommerce.chat.domain.model.Conversation;
import com.learnfirebase.ecommerce.chat.domain.model.ConversationId;
import com.learnfirebase.ecommerce.chat.domain.model.ParticipantId;
import java.util.Optional;

public interface ConversationRepository {
    Optional<Conversation> findById(ConversationId id);

    Optional<Conversation> findByParticipants(ParticipantId buyerId, ParticipantId sellerId);

    Conversation save(Conversation conversation);
}
