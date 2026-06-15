package com.learnfirebase.ecommerce.report.infrastructure.source;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class RawEventReaderAdapterTest {
    private JdbcTemplate jdbcTemplate;
    private RawEventReaderAdapter adapter;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
            "jdbc:h2:mem:report-" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
            "sa",
            ""
        );
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("""
            CREATE TABLE orders (
                id VARCHAR(255) PRIMARY KEY,
                status VARCHAR(50),
                total_amount VARCHAR(50),
                created_at TIMESTAMP
            )
            """);
        adapter = new RawEventReaderAdapter(jdbcTemplate);
    }

    @Test
    void countsRevenueOnlyForReportableOrdersCreatedOnTheRequestedDay() {
        LocalDate date = LocalDate.of(2026, 6, 15);

        insertOrder("paid", "PAID", "100000", date.atTime(0, 0));
        insertOrder("shipping", "SHIPPING", "25000.50", date.atTime(12, 30));
        insertOrder("delivered", "DELIVERED", "49999.50", date.atTime(23, 59, 59));
        insertOrder("pending", "PENDING", "999999", date.atTime(8, 0));
        insertOrder("cancelled", "CANCELLED", "999999", date.atTime(9, 0));
        insertOrder("returned", "RETURNED", "999999", date.atTime(10, 0));
        insertOrder("previous-day", "DELIVERED", "999999", date.minusDays(1).atTime(23, 59, 59));
        insertOrder("next-day", "DELIVERED", "999999", date.plusDays(1).atStartOfDay());

        assertThat(adapter.totalOrdersForDay(date)).isEqualTo(3);
        assertThat(adapter.totalRevenueForDay(date)).isEqualTo(175000.0);
    }

    @Test
    void returnsZeroWhenTheDayHasNoReportableOrders() {
        LocalDate date = LocalDate.of(2026, 6, 15);

        insertOrder("pending", "PENDING", "100000", date.atTime(8, 0));

        assertThat(adapter.totalOrdersForDay(date)).isZero();
        assertThat(adapter.totalRevenueForDay(date)).isZero();
    }

    private void insertOrder(String id, String status, String totalAmount, LocalDateTime createdAt) {
        jdbcTemplate.update(
            "INSERT INTO orders (id, status, total_amount, created_at) VALUES (?, ?, ?, ?)",
            id,
            status,
            totalAmount,
            createdAt
        );
    }
}
