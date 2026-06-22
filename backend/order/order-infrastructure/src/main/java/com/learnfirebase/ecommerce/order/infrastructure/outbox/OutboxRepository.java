package com.learnfirebase.ecommerce.order.infrastructure.outbox;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxRepository extends JpaRepository<OutboxEntity, String> {
    
    @Query(value = "SELECT * FROM order_outbox o WHERE o.status = :status FOR UPDATE SKIP LOCKED LIMIT :limit", nativeQuery = true)
    List<OutboxEntity> findAndLockByStatus(@Param("status") String status, @Param("limit") int limit);
    
    List<OutboxEntity> findByStatus(OutboxStatus status);
    
    Page<OutboxEntity> findByStatusIn(List<OutboxStatus> statuses, Pageable pageable);
}
