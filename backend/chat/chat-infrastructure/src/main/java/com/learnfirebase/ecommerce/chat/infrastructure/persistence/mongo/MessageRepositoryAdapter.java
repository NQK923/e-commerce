package com.learnfirebase.ecommerce.chat.infrastructure.persistence.mongo;

import com.learnfirebase.ecommerce.chat.domain.model.ConversationId;
import com.learnfirebase.ecommerce.chat.domain.model.Message;
import com.learnfirebase.ecommerce.chat.domain.model.MessageId;
import com.learnfirebase.ecommerce.chat.domain.model.MessageStatus;
import com.learnfirebase.ecommerce.chat.domain.model.ParticipantId;
import com.learnfirebase.ecommerce.chat.domain.repository.MessageRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class MessageRepositoryAdapter implements MessageRepository {

    private final MessageMongoRepository messageMongoRepository;

    public MessageRepositoryAdapter(MessageMongoRepository messageMongoRepository) {
        this.messageMongoRepository = messageMongoRepository;
    }

    @Override
    public Message save(Message message) {
        MessageDocument saved = messageMongoRepository.save(java.util.Objects.requireNonNull(toDocument(message)));
        return toDomain(saved);
    }

    @Override
    public java.util.Optional<Message> findById(MessageId id) {
        return messageMongoRepository.findById(java.util.Objects.requireNonNull(id.getValue())).map(this::toDomain);
    }

    @Override
    public List<Message> findRecentByConversation(ConversationId conversationId, int limit) {
        var pageable = PageRequest.of(0, Math.max(limit, 1), Sort.by(Sort.Direction.DESC, "sentAt"));
        return messageMongoRepository.findByConversationId(conversationId.getValue(), pageable)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countUnreadByConversationAndReceiver(ConversationId conversationId, ParticipantId receiverId) {
        return messageMongoRepository.countByConversationIdAndReceiverIdAndStatusIsNot(
                conversationId.getValue(),
                receiverId.getValue(),
                MessageStatus.READ);
    }

    private Message toDomain(MessageDocument document) {
        return Message.builder()
                .id(MessageId.of(document.getId()))
                .conversationId(ConversationId.of(document.getConversationId()))
                .senderId(ParticipantId.of(document.getSenderId()))
                .receiverId(ParticipantId.of(document.getReceiverId()))
                .content(document.getContent())
                .sentAt(document.getSentAt())
                .status(document.getStatus())
                .build();
    }

    private MessageDocument toDocument(Message message) {
        return MessageDocument.builder()
                .id(message.getId().getValue())
                .conversationId(message.getConversationId().getValue())
                .senderId(message.getSenderId().getValue())
                .receiverId(message.getReceiverId().getValue())
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .status(message.getStatus())
                .build();
    }
}
