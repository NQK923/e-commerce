package com.learnfirebase.ecommerce.chat.infrastructure.persistence.mongo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageMongoRepository extends MongoRepository<MessageDocument, String> {
    Page<MessageDocument> findByConversationId(String conversationId, Pageable pageable);
}
