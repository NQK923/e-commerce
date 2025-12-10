package com.learnfirebase.ecommerce.chat.infrastructure.persistence.jpa;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chat_conversation")
@Getter
@Setter
@NoArgsConstructor
public class ConversationJpaEntity {
    @Id
    private String id;

    @ElementCollection
    @CollectionTable(
            name = "chat_conversation_participants",
            joinColumns = @JoinColumn(name = "conversation_id")
    )
    @Column(name = "participant_id")
    private Set<String> participantIds;

    private Instant createdAt;
}
