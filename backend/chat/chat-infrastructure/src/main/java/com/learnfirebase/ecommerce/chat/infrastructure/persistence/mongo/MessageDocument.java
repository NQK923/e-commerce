package com.learnfirebase.ecommerce.chat.infrastructure.persistence.mongo;

import com.learnfirebase.ecommerce.chat.domain.model.MessageStatus;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "chat_message")
@CompoundIndex(name = "idx_chat_message_conversation_sent_at", def = "{'conversationId': 1, 'sentAt': -1}")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDocument {
    @Id
    private String id;
    private String conversationId;
    private String senderId;
    private String receiverId;
    private String content;
    private Instant sentAt;
    private MessageStatus status;
}
