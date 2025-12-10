package com.learnfirebase.ecommerce.chat.infrastructure.persistence.jpa;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConversationJpaRepository extends JpaRepository<ConversationJpaEntity, String> {

    @Query("select c from ConversationJpaEntity c where :userA member of c.participantIds and :userB member of c.participantIds")
    Optional<ConversationJpaEntity> findByParticipants(@Param("userA") String userA,
                                                       @Param("userB") String userB);

    @Query("select c from ConversationJpaEntity c where :participantId member of c.participantIds")
    java.util.List<ConversationJpaEntity> findByParticipant(@Param("participantId") String participantId);
}
