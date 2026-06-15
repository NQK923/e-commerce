package com.learnfirebase.ecommerce.report.infrastructure.persistence;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.learnfirebase.ecommerce.report.application.port.out.ReportRepository;
import com.learnfirebase.ecommerce.report.domain.model.DailySalesReport;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ReportRepositoryImpl implements ReportRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void save(DailySalesReport report) {
        jdbcTemplate.update("""
            INSERT INTO daily_sales_reports (report_date, total_revenue, total_orders, updated_at)
            VALUES (?, ?, ?, NOW())
            ON CONFLICT (report_date) DO UPDATE SET
                total_revenue = EXCLUDED.total_revenue,
                total_orders = EXCLUDED.total_orders,
                updated_at = NOW()
            """, report.getDate(), report.getTotalSales(), report.getTotalOrders());
    }
}
