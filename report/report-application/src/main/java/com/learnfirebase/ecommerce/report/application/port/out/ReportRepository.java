package com.learnfirebase.ecommerce.report.application.port.out;

import com.learnfirebase.ecommerce.report.domain.model.DailySalesReport;

public interface ReportRepository {
    void save(DailySalesReport report);
}
