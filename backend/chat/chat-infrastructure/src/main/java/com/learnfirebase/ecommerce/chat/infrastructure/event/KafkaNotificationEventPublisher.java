package com.learnfirebase.ecommerce.chat.infrastructure.event;

import com.learnfirebase.ecommerce.chat.application.port.out.NotificationEventPort;
import com.learnfirebase.ecommerce.chat.domain.model.Message;
import java.time.Instant;
import java.util.Objects;
import lombok.Builder;
import lombok.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaNotificationEventPublisher implements NotificationEventPort {

    private static final String TOPIC = "chat.offline.message";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaNotificationEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishOfflineMessage(Message message) {
        OfflineMessageEvent event = OfflineMessageEvent.builder()
                .messageId(message.getId().getValue())
                .conversationId(message.getConversationId().getValue())
                .receiverId(message.getReceiverId().getValue())
                .senderId(message.getSenderId().getValue())
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .build();
        kafkaTemplate.send(TOPIC, Objects.requireNonNull(message.getReceiverId().getValue()), event);
    }

    @Value
    @Builder
    static class OfflineMessageEvent {
        String messageId;
        String conversationId;
        String senderId;
        String receiverId;
        String content;
        Instant sentAt;
    }
}
