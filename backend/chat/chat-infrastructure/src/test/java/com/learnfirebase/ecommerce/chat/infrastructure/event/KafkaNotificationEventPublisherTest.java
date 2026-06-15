package com.learnfirebase.ecommerce.chat.infrastructure.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.time.Instant;

import com.learnfirebase.ecommerce.chat.domain.model.ConversationId;
import com.learnfirebase.ecommerce.chat.domain.model.Message;
import com.learnfirebase.ecommerce.chat.domain.model.MessageId;
import com.learnfirebase.ecommerce.chat.domain.model.MessageStatus;
import com.learnfirebase.ecommerce.chat.domain.model.ParticipantId;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

class KafkaNotificationEventPublisherTest {
    @Test
    void publishOfflineMessageSendsEventToReceiverKeyedKafkaTopic() {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, Object> kafkaTemplate = org.mockito.Mockito.mock(KafkaTemplate.class);
        KafkaNotificationEventPublisher publisher = new KafkaNotificationEventPublisher(kafkaTemplate);

        publisher.publishOfflineMessage(message());

        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(
            org.mockito.Mockito.eq("chat.offline.message"),
            org.mockito.Mockito.eq("seller-1"),
            payload.capture()
        );
        assertThat(payload.getValue()).isInstanceOf(KafkaNotificationEventPublisher.OfflineMessageEvent.class);
        KafkaNotificationEventPublisher.OfflineMessageEvent event =
            (KafkaNotificationEventPublisher.OfflineMessageEvent) payload.getValue();
        assertThat(event.getMessageId()).isEqualTo("message-1");
        assertThat(event.getConversationId()).isEqualTo("conversation-1");
        assertThat(event.getSenderId()).isEqualTo("buyer-1");
        assertThat(event.getReceiverId()).isEqualTo("seller-1");
        assertThat(event.getContent()).isEqualTo("hello");
        assertThat(event.getSentAt()).isEqualTo(Instant.parse("2026-06-15T00:00:00Z"));
    }

    private Message message() {
        return Message.builder()
            .id(MessageId.of("message-1"))
            .conversationId(ConversationId.of("conversation-1"))
            .senderId(ParticipantId.of("buyer-1"))
            .receiverId(ParticipantId.of("seller-1"))
            .content("hello")
            .sentAt(Instant.parse("2026-06-15T00:00:00Z"))
            .status(MessageStatus.PENDING)
            .build();
    }
}
