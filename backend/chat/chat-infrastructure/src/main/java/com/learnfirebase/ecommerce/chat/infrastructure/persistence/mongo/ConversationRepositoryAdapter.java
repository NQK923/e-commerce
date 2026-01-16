package com.learnfirebase.ecommerce.chat.infrastructure.persistence.mongo;

import java.util.Objects;

import com.learnfirebase.ecommerce.chat.domain.model.Conversation;
import com.learnfirebase.ecommerce.chat.domain.model.ConversationId;
import com.learnfirebase.ecommerce.chat.domain.model.ParticipantId;
import com.learnfirebase.ecommerce.chat.domain.repository.ConversationRepository;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class ConversationRepositoryAdapter implements ConversationRepository {

    private final ConversationMongoRepository conversationMongoRepository;

    public ConversationRepositoryAdapter(ConversationMongoRepository conversationMongoRepository) {
        this.conversationMongoRepository = conversationMongoRepository;
    }

    @Override
    public Optional<Conversation> findById(ConversationId id) {
        return conversationMongoRepository.findById(Objects.requireNonNull(id.getValue())).map(this::toDomain);
    }

    @Override
    public Optional<Conversation> findByParticipants(ParticipantId buyerId, ParticipantId sellerId) {
        return conversationMongoRepository
                .findByParticipants(Objects.requireNonNull(buyerId.getValue()),
                        Objects.requireNonNull(sellerId.getValue()))
                .map(this::toDomain);
    }

    @Override
    public java.util.List<Conversation> findByParticipant(ParticipantId participantId) {
        return conversationMongoRepository.findByParticipantsContains(Objects.requireNonNull(participantId.getValue()))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Conversation save(Conversation conversation) {
        ConversationDocument saved = conversationMongoRepository.save(Objects.requireNonNull(toDocument(conversation)));
        return toDomain(saved);
    }

    private Conversation toDomain(ConversationDocument document) {
        Set<ParticipantId> participants = document.getParticipants().stream()
                .map(ParticipantId::of)
                .collect(Collectors.toSet());

        return Conversation.builder()
                .id(ConversationId.of(document.getId()))
                .participants(participants)
                .createdAt(document.getCreatedAt())
                .build();
    }

    private ConversationDocument toDocument(Conversation conversation) {
        return ConversationDocument.builder()
                .id(conversation.getId().getValue())
                .participants(conversation.getParticipants().stream()
                        .map(ParticipantId::getValue)
                        .collect(Collectors.toSet()))
                .createdAt(conversation.getCreatedAt())
                .build();
    }
}
