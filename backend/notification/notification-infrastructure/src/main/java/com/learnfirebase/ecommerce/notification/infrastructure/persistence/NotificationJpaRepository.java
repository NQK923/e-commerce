package com.learnfirebase.ecommerce.notification.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.learnfirebase.ecommerce.notification.domain.model.NotificationStatus;

public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, String> {
    List<NotificationEntity> findTop20ByUserIdOrderByCreatedAtDesc(String userId);

    long countByUserIdAndStatus(String userId, NotificationStatus status);

    @Modifying
    @Query("update NotificationEntity n set n.status = :status, n.readAt = CURRENT_TIMESTAMP where n.id = :id")
    void updateStatus(@Param("id") String id, @Param("status") NotificationStatus status);
}
