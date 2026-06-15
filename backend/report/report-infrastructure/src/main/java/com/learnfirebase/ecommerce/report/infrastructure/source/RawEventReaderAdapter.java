package com.learnfirebase.ecommerce.report.infrastructure.source;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.learnfirebase.ecommerce.report.application.port.out.RawEventReaderPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RawEventReaderAdapter implements RawEventReaderPort {
    private static final String REPORTABLE_ORDER_STATUSES = "'PAID','SHIPPING','DELIVERED'";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public double totalRevenueForDay(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        Double revenue = jdbcTemplate.queryForObject("""
            SELECT COALESCE(SUM(CAST(total_amount AS DECIMAL(19, 2))), 0)
            FROM orders
            WHERE status IN (%s)
              AND created_at >= ?
              AND created_at < ?
              AND total_amount IS NOT NULL
            """.formatted(REPORTABLE_ORDER_STATUSES), Double.class, start, end);
        return revenue != null ? revenue : 0.0;
    }

    @Override
    public int totalOrdersForDay(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        Integer orders = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM orders
            WHERE status IN (%s)
              AND created_at >= ?
              AND created_at < ?
            """.formatted(REPORTABLE_ORDER_STATUSES), Integer.class, start, end);
        return orders != null ? orders : 0;
    }
}
