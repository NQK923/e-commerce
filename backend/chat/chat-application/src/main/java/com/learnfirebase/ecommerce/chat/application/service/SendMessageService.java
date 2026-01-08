package com.learnfirebase.ecommerce.chat.application.service;

import com.learnfirebase.ecommerce.chat.application.dto.SendMessageCommand;
import com.learnfirebase.ecommerce.chat.application.dto.SendMessageResult;
import com.learnfirebase.ecommerce.chat.application.port.out.MessageDeliveryPort;
import com.learnfirebase.ecommerce.chat.application.port.out.NotificationEventPort;
import com.learnfirebase.ecommerce.chat.application.port.out.PresencePort;
import com.learnfirebase.ecommerce.chat.application.usecase.SendMessageUseCase;
import com.learnfirebase.ecommerce.chat.domain.model.Conversation;
import com.learnfirebase.ecommerce.chat.domain.model.ConversationId;
import com.learnfirebase.ecommerce.chat.domain.model.Message;
import com.learnfirebase.ecommerce.chat.domain.model.MessageId;
import com.learnfirebase.ecommerce.chat.domain.model.MessageStatus;
import com.learnfirebase.ecommerce.chat.domain.model.ParticipantId;
import com.learnfirebase.ecommerce.chat.domain.repository.ConversationRepository;
import com.learnfirebase.ecommerce.chat.domain.repository.MessageRepository;
import java.time.Instant;
import java.util.Optional;

import com.learnfirebase.ecommerce.chat.application.dto.ChatMessageDto;

public class SendMessageService implements SendMessageUseCase {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final MessageDeliveryPort messageDeliveryPort;
    private final PresencePort presencePort;
    private final NotificationEventPort notificationEventPort;

    public SendMessageService(ConversationRepository conversationRepository,
                              MessageRepository messageRepository,
                              MessageDeliveryPort messageDeliveryPort,
                              PresencePort presencePort,
                              NotificationEventPort notificationEventPort) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.messageDeliveryPort = messageDeliveryPort;
        this.presencePort = presencePort;
        this.notificationEventPort = notificationEventPort;
    }

    @Override
    public SendMessageResult send(SendMessageCommand command) {
        validate(command);

        ParticipantId sender = ParticipantId.of(command.getSenderId());
        ParticipantId receiver = ParticipantId.of(command.getReceiverId());

        Conversation conversation = resolveConversation(command.getConversationId(), sender, receiver);

        Message message = Message.builder()
                .id(MessageId.newId())
                .conversationId(conversation.getId())
                .senderId(sender)
                .receiverId(receiver)
                .content(command.getContent().trim())
                .sentAt(Instant.now())
                .status(MessageStatus.PENDING)
                .build();

        Message persisted = messageRepository.save(message);

        boolean receiverOnline = presencePort.isOnline(receiver.getValue());
        Message toDeliver = receiverOnline ? messageRepository.save(persisted.delivered()) : persisted;

        messageDeliveryPort.deliverToUser(receiver.getValue(), toDeliver);

        if (!receiverOnline) {
            notificationEventPort.publishOfflineMessage(toDeliver);
        }

        return new SendMessageResult(toDto(toDeliver), receiverOnline);
    }

    private ChatMessageDto toDto(Message message) {
        return ChatMessageDto.builder()
                .id(message.getId().getValue())
                .conversationId(message.getConversationId().getValue())
                .senderId(message.getSenderId().getValue())
                .receiverId(message.getReceiverId().getValue())
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .status(message.getStatus())
                .build();
    }

    private Conversation resolveConversation(String conversationIdRaw, ParticipantId sender, ParticipantId receiver) {
        if (conversationIdRaw != null && !conversationIdRaw.isBlank()) {
            return conversationRepository.findById(ConversationId.of(conversationIdRaw))
                    .orElseThrow(() -> new IllegalArgumentException("Conversation not found: " + conversationIdRaw));
        }

        Optional<Conversation> existing = conversationRepository.findByParticipants(sender, receiver);
        if (existing.isPresent()) {
            return existing.get();
        }

        Conversation created = Conversation.builder()
                .id(ConversationId.newId())
                .participants(java.util.Set.of(sender, receiver))
                .createdAt(Instant.now())
                .build();

        return conversationRepository.save(created);
    }

    private void validate(SendMessageCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Command must not be null");
        }
        if (command.getSenderId() == null || command.getSenderId().isBlank()) {
            throw new IllegalArgumentException("Sender is required");
        }
        if (command.getReceiverId() == null || command.getReceiverId().isBlank()) {
            throw new IllegalArgumentException("Receiver is required");
        }
        if (command.getContent() == null || command.getContent().isBlank()) {
            throw new IllegalArgumentException("Message content must not be empty");
        }
    }
}
