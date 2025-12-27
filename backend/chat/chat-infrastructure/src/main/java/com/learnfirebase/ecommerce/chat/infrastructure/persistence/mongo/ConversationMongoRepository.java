package com.learnfirebase.ecommerce.chat.infrastructure.persistence.mongo;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ConversationMongoRepository extends MongoRepository<ConversationDocument, String> {

    @Query("{ 'participants': { $all: [ ?0, ?1 ] } }")
    Optional<ConversationDocument> findByParticipants(String userA, String userB);

    List<ConversationDocument> findByParticipantsContains(String participantId);
}
