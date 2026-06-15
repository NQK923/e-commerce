package com.learnfirebase.ecommerce.chat.infrastructure.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.time.Instant;

import com.learnfirebase.ecommerce.chat.application.dto.ChatMessageDto;
import com.learnfirebase.ecommerce.chat.domain.model.ConversationId;
import com.learnfirebase.ecommerce.chat.domain.model.Message;
import com.learnfirebase.ecommerce.chat.domain.model.MessageId;
import com.learnfirebase.ecommerce.chat.domain.model.MessageStatus;
import com.learnfirebase.ecommerce.chat.domain.model.ParticipantId;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

class StompMessageDeliveryAdapterTest {
    @Test
    void deliverToUserSendsChatMessageDtoToUserQueue() {
        SimpMessagingTemplate simpMessagingTemplate = org.mockito.Mockito.mock(SimpMessagingTemplate.class);
        StompMessageDeliveryAdapter adapter = new StompMessageDeliveryAdapter(simpMessagingTemplate);

        adapter.deliverToUser("seller-1", message(MessageStatus.DELIVERED));

        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);
        verify(simpMessagingTemplate).convertAndSendToUser(
            org.mockito.Mockito.eq("seller-1"),
            org.mockito.Mockito.eq("/queue/chat/messages"),
            payload.capture()
        );
        assertThat(payload.getValue()).isInstanceOf(ChatMessageDto.class);
        ChatMessageDto dto = (ChatMessageDto) payload.getValue();
        assertThat(dto.getId()).isEqualTo("message-1");
        assertThat(dto.getConversationId()).isEqualTo("conversation-1");
        assertThat(dto.getSenderId()).isEqualTo("buyer-1");
        assertThat(dto.getReceiverId()).isEqualTo("seller-1");
        assertThat(dto.getContent()).isEqualTo("hello");
        assertThat(dto.getStatus()).isEqualTo(MessageStatus.DELIVERED);
    }

    private Message message(MessageStatus status) {
        return Message.builder()
            .id(MessageId.of("message-1"))
            .conversationId(ConversationId.of("conversation-1"))
            .senderId(ParticipantId.of("buyer-1"))
            .receiverId(ParticipantId.of("seller-1"))
            .content("hello")
            .sentAt(Instant.parse("2026-06-15T00:00:00Z"))
            .status(status)
            .build();
    }
}
