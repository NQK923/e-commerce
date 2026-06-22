package com.learnfirebase.ecommerce.chat.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.learnfirebase.ecommerce.chat.application.port.out.MessageDeliveryPort;
import com.learnfirebase.ecommerce.chat.domain.model.Conversation;
import com.learnfirebase.ecommerce.chat.domain.model.ConversationId;
import com.learnfirebase.ecommerce.chat.domain.model.Message;
import com.learnfirebase.ecommerce.chat.domain.model.MessageId;
import com.learnfirebase.ecommerce.chat.domain.model.MessageStatus;
import com.learnfirebase.ecommerce.chat.domain.model.ParticipantId;
import com.learnfirebase.ecommerce.chat.domain.repository.ConversationRepository;
import com.learnfirebase.ecommerce.chat.domain.repository.MessageRepository;
import com.learnfirebase.ecommerce.common.domain.AccessDeniedDomainException;
import com.learnfirebase.ecommerce.common.domain.ResourceNotFoundDomainException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatQueryServiceTest {
    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private MessageDeliveryPort messageDeliveryPort;

    private ChatQueryService service;

    @BeforeEach
    void setUp() {
        service = new ChatQueryService(conversationRepository, messageRepository, messageDeliveryPort);
    }

    @Test
    void listMessagesThrowsNotFoundWhenConversationDoesNotExist() {
        when(conversationRepository.findById(any(ConversationId.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listMessages("conv-1", "user-1", 50))
            .isInstanceOf(ResourceNotFoundDomainException.class)
            .hasMessageContaining("Conversation not found: conv-1");
    }

    @Test
    void listMessagesThrowsAccessDeniedWhenUserNotParticipant() {
        Conversation conversation = Conversation.builder()
            .id(ConversationId.of("conv-1"))
            .participants(Set.of(ParticipantId.of("user-2"), ParticipantId.of("user-3")))
            .createdAt(Instant.now())
            .build();
        when(conversationRepository.findById(ConversationId.of("conv-1"))).thenReturn(Optional.of(conversation));

        assertThatThrownBy(() -> service.listMessages("conv-1", "user-1", 50))
            .isInstanceOf(AccessDeniedDomainException.class)
            .hasMessageContaining("is not a participant in conversation conv-1");
    }

    @Test
    void listMessagesReturnsMessagesWhenUserIsParticipant() {
        Conversation conversation = Conversation.builder()
            .id(ConversationId.of("conv-1"))
            .participants(Set.of(ParticipantId.of("user-1"), ParticipantId.of("user-2")))
            .createdAt(Instant.now())
            .build();
        when(conversationRepository.findById(ConversationId.of("conv-1"))).thenReturn(Optional.of(conversation));

        Message message = Message.builder()
            .id(MessageId.of("msg-1"))
            .conversationId(ConversationId.of("conv-1"))
            .senderId(ParticipantId.of("user-1"))
            .receiverId(ParticipantId.of("user-2"))
            .content("hello")
            .sentAt(Instant.now())
            .status(MessageStatus.DELIVERED)
            .build();

        when(messageRepository.findRecentByConversation(ConversationId.of("conv-1"), 50))
            .thenReturn(List.of(message));

        var messages = service.listMessages("conv-1", "user-1", 50);

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).getContent()).isEqualTo("hello");
    }
}
