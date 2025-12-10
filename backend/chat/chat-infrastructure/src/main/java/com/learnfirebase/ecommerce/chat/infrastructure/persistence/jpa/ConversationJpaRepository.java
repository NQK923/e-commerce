package com.learnfirebase.ecommerce.chat.infrastructure.persistence.jpa;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ConversationJpaRepository extends JpaRepository<ConversationJpaEntity, String> {

    @Query("select c from ConversationJpaEntity c where :userA member of c.participantIds and :userB member of c.participantIds")
    Optional<ConversationJpaEntity> findByParticipants(String userA, String userB);

    @Query("select c from ConversationJpaEntity c where :participantId member of c.participantIds")
    java.util.List<ConversationJpaEntity> findByParticipant(String participantId);
}
