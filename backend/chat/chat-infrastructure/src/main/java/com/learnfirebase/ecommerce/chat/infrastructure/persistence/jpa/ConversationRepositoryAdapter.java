package com.learnfirebase.ecommerce.chat.infrastructure.persistence.jpa;

import com.learnfirebase.ecommerce.chat.domain.model.Conversation;
import com.learnfirebase.ecommerce.chat.domain.model.ConversationId;
import com.learnfirebase.ecommerce.chat.domain.model.ParticipantId;
import com.learnfirebase.ecommerce.chat.domain.repository.ConversationRepository;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ConversationRepositoryAdapter implements ConversationRepository {

    private final ConversationJpaRepository conversationJpaRepository;

    public ConversationRepositoryAdapter(ConversationJpaRepository conversationJpaRepository) {
        this.conversationJpaRepository = conversationJpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Conversation> findById(ConversationId id) {
        return conversationJpaRepository.findById(id.getValue()).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Conversation> findByParticipants(ParticipantId buyerId, ParticipantId sellerId) {
        return conversationJpaRepository.findByParticipants(buyerId.getValue(), sellerId.getValue())
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<Conversation> findByParticipant(ParticipantId participantId) {
        return conversationJpaRepository.findByParticipant(participantId.getValue())
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Conversation save(Conversation conversation) {
        ConversationJpaEntity entity = toEntity(conversation);
        ConversationJpaEntity saved = conversationJpaRepository.save(entity);
        return toDomain(saved);
    }

    private Conversation toDomain(ConversationJpaEntity entity) {
        return Conversation.builder()
                .id(ConversationId.of(entity.getId()))
                .participants(entity.getParticipantIds().stream().map(ParticipantId::of).collect(Collectors.toSet()))
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private ConversationJpaEntity toEntity(Conversation conversation) {
        ConversationJpaEntity entity = new ConversationJpaEntity();
        entity.setId(conversation.getId().getValue());
        entity.setParticipantIds(conversation.getParticipants().stream()
                .map(ParticipantId::getValue)
                .collect(Collectors.toSet()));
        entity.setCreatedAt(conversation.getCreatedAt());
        return entity;
    }
}
