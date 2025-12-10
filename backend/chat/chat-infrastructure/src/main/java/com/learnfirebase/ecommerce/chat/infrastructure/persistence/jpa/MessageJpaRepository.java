package com.learnfirebase.ecommerce.chat.infrastructure.persistence.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageJpaRepository extends JpaRepository<MessageJpaEntity, String> {
    Page<MessageJpaEntity> findByConversationId(String conversationId, Pageable pageable);
}
