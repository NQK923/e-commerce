package com.learnfirebase.ecommerce.report.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;

import com.learnfirebase.ecommerce.report.domain.model.DailySalesReport;

class ReportRepositoryImplTest {
    @Test
    void saveUpsertsTheDailySalesSnapshot() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        ReportRepositoryImpl repository = new ReportRepositoryImpl(jdbcTemplate);
        DailySalesReport report = DailySalesReport.builder()
            .date(LocalDate.of(2026, 6, 15))
            .totalSales(175000.0)
            .totalOrders(3)
            .build();

        repository.save(report);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).update(
            sqlCaptor.capture(),
            eq(report.getDate()),
            eq(report.getTotalSales()),
            eq(report.getTotalOrders())
        );
        assertThat(sqlCaptor.getValue())
            .contains("INSERT INTO daily_sales_reports")
            .contains("ON CONFLICT (report_date) DO UPDATE");
    }
}
