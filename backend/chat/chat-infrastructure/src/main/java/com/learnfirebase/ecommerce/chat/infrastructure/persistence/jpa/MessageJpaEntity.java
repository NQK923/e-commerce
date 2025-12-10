package com.learnfirebase.ecommerce.chat.infrastructure.persistence.jpa;

import com.learnfirebase.ecommerce.chat.domain.model.MessageStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chat_message", indexes = {
        @Index(name = "idx_chat_message_conversation_sent_at", columnList = "conversationId, sentAt")
})
@Getter
@Setter
@NoArgsConstructor
public class MessageJpaEntity {
    @Id
    private String id;
    private String conversationId;
    private String senderId;
    private String receiverId;
    private String content;
    private Instant sentAt;
    @Enumerated(EnumType.STRING)
    private MessageStatus status;
}
