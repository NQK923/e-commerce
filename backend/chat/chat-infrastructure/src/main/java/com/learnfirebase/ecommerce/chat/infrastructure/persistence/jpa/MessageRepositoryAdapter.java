package com.learnfirebase.ecommerce.chat.infrastructure.persistence.jpa;

import com.learnfirebase.ecommerce.chat.domain.model.ConversationId;
import com.learnfirebase.ecommerce.chat.domain.model.Message;
import com.learnfirebase.ecommerce.chat.domain.model.MessageId;
import com.learnfirebase.ecommerce.chat.domain.model.ParticipantId;
import com.learnfirebase.ecommerce.chat.domain.repository.MessageRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class MessageRepositoryAdapter implements MessageRepository {

    private final MessageJpaRepository messageJpaRepository;

    public MessageRepositoryAdapter(MessageJpaRepository messageJpaRepository) {
        this.messageJpaRepository = messageJpaRepository;
    }

    @Override
    public Message save(Message message) {
        MessageJpaEntity entity = toEntity(message);
        MessageJpaEntity saved = messageJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<Message> findById(MessageId id) {
        return messageJpaRepository.findById(id.getValue()).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> findRecentByConversation(ConversationId conversationId, int limit) {
        var pageable = PageRequest.of(0, Math.max(limit, 1), Sort.by(Sort.Direction.DESC, "sentAt"));
        return messageJpaRepository.findByConversationId(conversationId.getValue(), pageable)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private Message toDomain(MessageJpaEntity entity) {
        return Message.builder()
                .id(MessageId.of(entity.getId()))
                .conversationId(ConversationId.of(entity.getConversationId()))
                .senderId(ParticipantId.of(entity.getSenderId()))
                .receiverId(ParticipantId.of(entity.getReceiverId()))
                .content(entity.getContent())
                .sentAt(entity.getSentAt())
                .status(entity.getStatus())
                .build();
    }

    private MessageJpaEntity toEntity(Message message) {
        MessageJpaEntity entity = new MessageJpaEntity();
        entity.setId(message.getId().getValue());
        entity.setConversationId(message.getConversationId().getValue());
        entity.setSenderId(message.getSenderId().getValue());
        entity.setReceiverId(message.getReceiverId().getValue());
        entity.setContent(message.getContent());
        entity.setSentAt(message.getSentAt());
        entity.setStatus(message.getStatus());
        return entity;
    }
}
