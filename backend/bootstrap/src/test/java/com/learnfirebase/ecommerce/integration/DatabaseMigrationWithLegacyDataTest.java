package com.learnfirebase.ecommerce.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
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

import com.learnfirebase.ecommerce.bootstrap.EcommerceApplication;

@DataJpaTest(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.datasource.hikari.maximum-pool-size=10"
})
@ContextConfiguration(classes = EcommerceApplication.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
@org.springframework.data.jpa.repository.config.EnableJpaRepositories(basePackages = "com.learnfirebase.ecommerce")
@org.springframework.boot.autoconfigure.domain.EntityScan(basePackages = "com.learnfirebase.ecommerce")
@org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
class DatabaseMigrationWithLegacyDataTest {

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
    private javax.sql.DataSource dataSource;

    @Test
    void migrateV1toV3WithLegacyDataSuccessfully() {
        // 1. Configure and run Flyway migration up to version 1 (baseline schema)
        org.flywaydb.core.Flyway flywayV1 = org.flywaydb.core.Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .target("1")
                .load();
        flywayV1.migrate();

        org.springframework.jdbc.core.JdbcTemplate jdbcTemplate = new org.springframework.jdbc.core.JdbcTemplate(dataSource);

        // 2. Insert legacy data matching V1 VARCHAR fields
        String userId = "user-legacy-123";
        jdbcTemplate.update("INSERT INTO users (id, email, password, display_name, auth_provider, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, NOW(), NOW())",
                userId, "legacy@example.com", "password", "Legacy User", "LOCAL");

        String cartId = userId;
        jdbcTemplate.update("INSERT INTO carts (id) VALUES (?)", cartId);

        // cart_items price transitions
        jdbcTemplate.update("INSERT INTO cart_items (cart_id, product_id, variant_sku, quantity, price, currency) " +
                "VALUES (?, ?, ?, ?, ?, ?)",
                cartId, "prod-1", "sku-1", 2, "19.99", "USD"); // valid numeric string

        jdbcTemplate.update("INSERT INTO cart_items (cart_id, product_id, variant_sku, quantity, price, currency) " +
                "VALUES (?, ?, ?, ?, ?, ?)",
                cartId, "prod-2", "sku-2", 1, "", "USD"); // empty string

        jdbcTemplate.update("INSERT INTO cart_items (cart_id, product_id, variant_sku, quantity, price, currency) " +
                "VALUES (?, ?, ?, ?, ?, ?)",
                cartId, "prod-3", "sku-3", 1, null, "USD"); // null

        // orders total_amount and refund_amount transitions
        String orderId1 = "order-legacy-1";
        jdbcTemplate.update("INSERT INTO orders (id, user_id, status, currency, total_amount, refund_amount, return_status, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())",
                orderId1, userId, "PENDING", "USD", "150.50", "", "NONE"); // refund_amount empty string

        String orderId2 = "order-legacy-2";
        jdbcTemplate.update("INSERT INTO orders (id, user_id, status, currency, total_amount, refund_amount, return_status, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())",
                orderId2, userId, "COMPLETED", "USD", "99.99", "15.00", "NONE"); // both numeric strings

        // order_items price transition
        jdbcTemplate.update("INSERT INTO order_items (id, product_id, quantity, price, order_id, variant_sku, seller_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)",
                "item-1", "prod-1", 1, "150.50", orderId1, "sku-1", "seller-1");

        // 3. Migrate database to the latest schema version (V2 and V3)
        org.flywaydb.core.Flyway flywayLatest = org.flywaydb.core.Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();
        flywayLatest.migrate();

        // 4. Assert that the tables were safely cast and columns converted to numeric successfully
        // a. cart_items.price conversions
        BigDecimal cartPrice1 = jdbcTemplate.queryForObject(
                "SELECT price FROM cart_items WHERE product_id = 'prod-1'", BigDecimal.class);
        assertThat(cartPrice1).isEqualByComparingTo("19.99");

        BigDecimal cartPrice2 = jdbcTemplate.queryForObject(
                "SELECT price FROM cart_items WHERE product_id = 'prod-2'", BigDecimal.class);
        assertThat(cartPrice2).isNull();

        BigDecimal cartPrice3 = jdbcTemplate.queryForObject(
                "SELECT price FROM cart_items WHERE product_id = 'prod-3'", BigDecimal.class);
        assertThat(cartPrice3).isNull();

        // b. orders.total_amount & refund_amount conversions
        BigDecimal totalAmt1 = jdbcTemplate.queryForObject(
                "SELECT total_amount FROM orders WHERE id = 'order-legacy-1'", BigDecimal.class);
        assertThat(totalAmt1).isEqualByComparingTo("150.50");

        BigDecimal refundAmt1 = jdbcTemplate.queryForObject(
                "SELECT refund_amount FROM orders WHERE id = 'order-legacy-1'", BigDecimal.class);
        assertThat(refundAmt1).isNull();

        BigDecimal totalAmt2 = jdbcTemplate.queryForObject(
                "SELECT total_amount FROM orders WHERE id = 'order-legacy-2'", BigDecimal.class);
        assertThat(totalAmt2).isEqualByComparingTo("99.99");

        BigDecimal refundAmt2 = jdbcTemplate.queryForObject(
                "SELECT refund_amount FROM orders WHERE id = 'order-legacy-2'", BigDecimal.class);
        assertThat(refundAmt2).isEqualByComparingTo("15.00");

        // c. order_items.price conversion
        BigDecimal orderItemPrice = jdbcTemplate.queryForObject(
                "SELECT price FROM order_items WHERE id = 'item-1'", BigDecimal.class);
        assertThat(orderItemPrice).isEqualByComparingTo("150.50");
    }
}
