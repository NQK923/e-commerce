package com.learnfirebase.ecommerce.chat.application.service;

import com.learnfirebase.ecommerce.chat.application.dto.ChatMessageDto;
import com.learnfirebase.ecommerce.chat.application.dto.ConversationSummaryDto;
import com.learnfirebase.ecommerce.chat.application.port.in.GetConversationsUseCase;
import com.learnfirebase.ecommerce.chat.application.port.in.GetMessagesUseCase;
import com.learnfirebase.ecommerce.chat.application.port.in.MarkConversationReadUseCase;
import com.learnfirebase.ecommerce.chat.domain.model.Message;
import com.learnfirebase.ecommerce.chat.domain.model.MessageStatus;
import com.learnfirebase.ecommerce.chat.domain.model.ParticipantId;
import com.learnfirebase.ecommerce.chat.domain.repository.ConversationRepository;
import com.learnfirebase.ecommerce.chat.domain.repository.MessageRepository;
import java.util.Comparator;
import java.util.List;

public class ChatQueryService implements GetConversationsUseCase, GetMessagesUseCase, MarkConversationReadUseCase {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public ChatQueryService(ConversationRepository conversationRepository,
                            MessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    public List<ConversationSummaryDto> listForUser(String userId) {
        ParticipantId participantId = ParticipantId.of(userId);
        return conversationRepository.findByParticipant(participantId)
                .stream()
                .map(conversation -> {
                    var lastMessages = messageRepository.findRecentByConversation(conversation.getId(), 1);
                    ChatMessageDto lastMessage = lastMessages.stream()
                            .findFirst()
                            .map(this::toDto)
                            .orElse(null);
                    return ConversationSummaryDto.builder()
                            .id(conversation.getId().getValue())
                            .participants(conversation.getParticipants().stream()
                                    .map(pid -> ConversationSummaryDto.ParticipantDto.builder()
                                            .id(pid.getValue())
                                            .build())
                                    .toList())
                            .lastMessage(lastMessage)
                            .unreadCount(0) // could be computed via status in repository
                            .createdAt(conversation.getCreatedAt())
                            .build();
                })
                .toList();
    }

    @Override
    public List<ChatMessageDto> listMessages(String conversationId, int limit) {
        return messageRepository.findRecentByConversation(
                        com.learnfirebase.ecommerce.chat.domain.model.ConversationId.of(conversationId),
                        Math.max(limit, 1))
                .stream()
                .sorted(Comparator.comparing(Message::getSentAt))
                .map(this::toDto)
                .toList();
    }

    @Override
    public void markRead(String conversationId, String userId) {
        var messages = messageRepository.findRecentByConversation(
                com.learnfirebase.ecommerce.chat.domain.model.ConversationId.of(conversationId),
                200);
        messages.stream()
                .filter(m -> m.getReceiverId().equals(ParticipantId.of(userId)))
                .filter(m -> m.getStatus() != MessageStatus.READ)
                .forEach(m -> messageRepository.save(m.read()));
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
}
