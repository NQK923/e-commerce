package com.learnfirebase.ecommerce.product.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, String>, JpaSpecificationExecutor<ProductEntity> {
    @Modifying
    @Query("UPDATE ProductEntity p SET p.soldCount = COALESCE(p.soldCount, 0) + :quantity WHERE p.id = :id")
    void incrementSoldCount(@Param("id") String id, @Param("quantity") int quantity);
}
