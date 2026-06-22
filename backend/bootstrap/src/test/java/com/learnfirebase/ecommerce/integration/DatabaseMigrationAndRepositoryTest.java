package com.learnfirebase.ecommerce.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.jdbc.core.JdbcTemplate;
import com.learnfirebase.ecommerce.order.infrastructure.outbox.OutboxEntity;
import com.learnfirebase.ecommerce.order.infrastructure.outbox.OutboxRepository;
import com.learnfirebase.ecommerce.order.infrastructure.outbox.OutboxStatus;
import com.learnfirebase.ecommerce.product.infrastructure.persistence.ProductEntity;
import com.learnfirebase.ecommerce.product.infrastructure.persistence.ProductJpaRepository;
import com.learnfirebase.ecommerce.bootstrap.EcommerceApplication;

@DataJpaTest
@ContextConfiguration(classes = EcommerceApplication.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
@org.springframework.data.jpa.repository.config.EnableJpaRepositories(basePackages = "com.learnfirebase.ecommerce")
@org.springframework.boot.autoconfigure.domain.EntityScan(basePackages = "com.learnfirebase.ecommerce")
class DatabaseMigrationAndRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("ecommerce_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
    }

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private ProductJpaRepository productRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void flywayMigrationsShouldExecuteSuccessfully() {
        // If the context starts and we can inject repositories, Flyway migrations ran successfully.
        assertThat(outboxRepository).isNotNull();
        assertThat(productRepository).isNotNull();
    }

    @Test
    void outboxSkipLockedShouldReturnAndLockRows() {
        OutboxEntity entity1 = OutboxEntity.builder()
                .id(UUID.randomUUID().toString())
                .aggregateId("order-1")
                .type("OrderCreated")
                .payload("{}")
                .status(OutboxStatus.PENDING)
                .createdAt(java.time.Instant.now())
                .updatedAt(java.time.Instant.now())
                .build();
                
        outboxRepository.save(entity1);

        List<OutboxEntity> lockedRows = outboxRepository.findAndLockByStatus(OutboxStatus.PENDING.name(), 10);
        
        assertThat(lockedRows).isNotEmpty();
        assertThat(lockedRows.get(0).getId()).isEqualTo(entity1.getId());
    }

    @Test
    void productSoftDeleteShouldHideFromNormalQueries() {
        ProductEntity product = new ProductEntity();
        product.setId(UUID.randomUUID().toString());
        product.setSellerId("seller-1");
        product.setName("Test Product");
        product.setQuantity(100);
        product.setSoldCount(0);
        product.setCategoryId("cat-1");
        product.setCreatedAt(java.time.Instant.now());
        product.setUpdatedAt(java.time.Instant.now());
        
        productRepository.save(product);
        
        // Soft delete
        product.setDeletedAt(java.time.Instant.now());
        productRepository.save(product);
        
        // Verify it's not found in normal findAll
        List<ProductEntity> allProducts = productRepository.findAll();
        assertThat(allProducts).noneMatch(p -> p.getId().equals(product.getId()));
    }

    @Test
    void productDeleteByIdShouldSoftDelete() {
        ProductEntity product = new ProductEntity();
        String productId = UUID.randomUUID().toString();
        product.setId(productId);
        product.setSellerId("seller-1");
        product.setName("Soft Deleted Product");
        product.setQuantity(50);
        product.setSoldCount(0);
        product.setCategoryId("cat-1");
        product.setCreatedAt(java.time.Instant.now());
        product.setUpdatedAt(java.time.Instant.now());
        
        productRepository.save(product);
        
        // Delete using repository (which should invoke @SQLDelete)
        productRepository.deleteById(productId);
        productRepository.flush();
        
        // Verify it is hidden from normal JPA reads
        assertThat(productRepository.findById(productId)).isEmpty();
        
        // Verify via JdbcTemplate that the record still exists in the DB but with a non-null deleted_at
        String sql = "SELECT deleted_at FROM products WHERE id = ?";
        java.sql.Timestamp deletedAt = jdbcTemplate.queryForObject(sql, java.sql.Timestamp.class, productId);
        assertThat(deletedAt).isNotNull();
    }
}
