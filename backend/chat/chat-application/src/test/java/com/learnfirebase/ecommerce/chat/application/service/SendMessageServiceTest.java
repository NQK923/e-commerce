package com.learnfirebase.ecommerce.chat.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import com.learnfirebase.ecommerce.chat.application.dto.SendMessageCommand;
import com.learnfirebase.ecommerce.chat.application.port.out.MessageDeliveryPort;
import com.learnfirebase.ecommerce.chat.application.port.out.NotificationEventPort;
import com.learnfirebase.ecommerce.chat.application.port.out.PresencePort;
import com.learnfirebase.ecommerce.chat.domain.model.Conversation;
import com.learnfirebase.ecommerce.chat.domain.model.ConversationId;
import com.learnfirebase.ecommerce.chat.domain.model.Message;
import com.learnfirebase.ecommerce.chat.domain.model.MessageStatus;
import com.learnfirebase.ecommerce.chat.domain.model.ParticipantId;
import com.learnfirebase.ecommerce.chat.domain.repository.ConversationRepository;
import com.learnfirebase.ecommerce.chat.domain.repository.MessageRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SendMessageServiceTest {
    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private MessageDeliveryPort messageDeliveryPort;
    @Mock
    private PresencePort presencePort;
    @Mock
    private NotificationEventPort notificationEventPort;

    private SendMessageService service;

    @BeforeEach
    void setUp() {
        service = new SendMessageService(
            conversationRepository,
            messageRepository,
            messageDeliveryPort,
            presencePort,
            notificationEventPort
        );
    }

    @Test
    void sendDeliversMarkedDeliveredMessageWhenReceiverIsOnline() {
        Conversation conversation = conversation("conversation-1", "buyer-1", "seller-1");
        when(conversationRepository.findById(ConversationId.of("conversation-1"))).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(presencePort.isOnline("seller-1")).thenReturn(true);

        var result = service.send(command("conversation-1", "buyer-1", "seller-1", "  hello  "));

        assertThat(result.isReceiverOnline()).isTrue();
        assertThat(result.getPersistedMessage().getContent()).isEqualTo("hello");
        assertThat(result.getPersistedMessage().getStatus()).isEqualTo(MessageStatus.DELIVERED);
        ArgumentCaptor<Message> delivered = ArgumentCaptor.forClass(Message.class);
        verify(messageDeliveryPort).deliverToUser(org.mockito.Mockito.eq("seller-1"), delivered.capture());
        assertThat(delivered.getValue().getStatus()).isEqualTo(MessageStatus.DELIVERED);
        verify(notificationEventPort, never()).publishOfflineMessage(any());
    }

    @Test
    void sendPublishesOfflineNotificationWhenReceiverIsOffline() {
        Conversation conversation = conversation("conversation-1", "buyer-1", "seller-1");
        when(conversationRepository.findById(ConversationId.of("conversation-1"))).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(presencePort.isOnline("seller-1")).thenReturn(false);

        var result = service.send(command("conversation-1", "buyer-1", "seller-1", "hello"));

        assertThat(result.isReceiverOnline()).isFalse();
        assertThat(result.getPersistedMessage().getStatus()).isEqualTo(MessageStatus.PENDING);
        ArgumentCaptor<Message> offlineMessage = ArgumentCaptor.forClass(Message.class);
        verify(notificationEventPort).publishOfflineMessage(offlineMessage.capture());
        assertThat(offlineMessage.getValue().getReceiverId()).isEqualTo(ParticipantId.of("seller-1"));
        assertThat(offlineMessage.getValue().getStatus()).isEqualTo(MessageStatus.PENDING);
        verify(messageDeliveryPort).deliverToUser(org.mockito.Mockito.eq("seller-1"), any(Message.class));
    }

    @Test
    void sendRejectsConversationWhenSenderIsNotParticipant() {
        Conversation conversation = conversation("conversation-1", "buyer-1", "seller-1");
        when(conversationRepository.findById(ConversationId.of("conversation-1"))).thenReturn(Optional.of(conversation));

        assertThatThrownBy(() -> service.send(command("conversation-1", "attacker-1", "seller-1", "hello")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Sender and receiver must belong to the conversation");

        verify(messageRepository, never()).save(any());
        verify(messageDeliveryPort, never()).deliverToUser(any(), any());
        verify(notificationEventPort, never()).publishOfflineMessage(any());
    }

    @Test
    void sendRejectsConversationWhenReceiverIsNotParticipant() {
        Conversation conversation = conversation("conversation-1", "buyer-1", "seller-1");
        when(conversationRepository.findById(ConversationId.of("conversation-1"))).thenReturn(Optional.of(conversation));

        assertThatThrownBy(() -> service.send(command("conversation-1", "buyer-1", "attacker-1", "hello")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Sender and receiver must belong to the conversation");

        verify(messageRepository, never()).save(any());
        verify(messageDeliveryPort, never()).deliverToUser(any(), any());
        verify(notificationEventPort, never()).publishOfflineMessage(any());
    }

    @Test
    void sendCreatesConversationWhenNoExistingConversationFound() {
        when(conversationRepository.findByParticipants(ParticipantId.of("buyer-1"), ParticipantId.of("seller-1")))
            .thenReturn(Optional.empty());
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(presencePort.isOnline("seller-1")).thenReturn(false);

        var result = service.send(command(null, "buyer-1", "seller-1", "hello"));

        assertThat(result.getPersistedMessage().getConversationId()).isNotBlank();
        verify(conversationRepository).save(any(Conversation.class));
        verify(notificationEventPort).publishOfflineMessage(any(Message.class));
    }

    private SendMessageCommand command(String conversationId, String senderId, String receiverId, String content) {
        return SendMessageCommand.builder()
            .conversationId(conversationId)
            .senderId(senderId)
            .receiverId(receiverId)
            .content(content)
            .build();
    }

    private Conversation conversation(String conversationId, String participantA, String participantB) {
        return Conversation.builder()
            .id(ConversationId.of(conversationId))
            .participants(Set.of(ParticipantId.of(participantA), ParticipantId.of(participantB)))
            .createdAt(Instant.parse("2026-06-15T00:00:00Z"))
            .build();
    }
}
