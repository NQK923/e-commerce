package com.learnfirebase.ecommerce.chat.infrastructure.persistence.mongo;

import java.time.Instant;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "chat_conversation")
@CompoundIndex(name = "chat_conversation_participants_idx", def = "{'participants': 1}")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDocument {
    @Id
    private String id;
    private Set<String> participants;
    private Instant createdAt;
}
