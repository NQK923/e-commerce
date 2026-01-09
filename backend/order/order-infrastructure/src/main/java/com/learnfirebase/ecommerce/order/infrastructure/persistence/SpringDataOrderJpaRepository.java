package com.learnfirebase.ecommerce.order.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataOrderJpaRepository extends JpaRepository<JpaOrderEntity, String> {
    @Query("SELECT distinct o FROM JpaOrderEntity o JOIN o.items i WHERE i.sellerId = :sellerId")
    Page<JpaOrderEntity> findBySellerId(@Param("sellerId") String sellerId, Pageable pageable);

    @Query("SELECT count(distinct o) FROM JpaOrderEntity o JOIN o.items i WHERE i.sellerId = :sellerId")
    long countBySellerId(@Param("sellerId") String sellerId);
}
