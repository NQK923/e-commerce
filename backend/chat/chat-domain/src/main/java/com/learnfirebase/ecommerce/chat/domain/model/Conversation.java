package com.learnfirebase.ecommerce.chat.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Conversation {
    private final ConversationId id;
    private final Set<ParticipantId> participants;
    private final Instant createdAt;

    @Builder
    private Conversation(ConversationId id, Set<ParticipantId> participants, Instant createdAt) {
        if (participants == null || participants.size() != 2) {
            throw new IllegalArgumentException("Conversation must have exactly two participants");
        }
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.participants = Set.copyOf(participants);
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    public boolean includes(ParticipantId participantId) {
        return participants.contains(participantId);
    }
}
