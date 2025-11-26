package com.learnfirebase.ecommerce.report.infrastructure.persistence;

import org.springframework.stereotype.Repository;

import com.learnfirebase.ecommerce.report.application.port.out.ReportRepository;
import com.learnfirebase.ecommerce.report.domain.model.DailySalesReport;

@Repository
public class ReportRepositoryImpl implements ReportRepository {
    @Override
    public void save(DailySalesReport report) {
        // persist with JPA/Spring Batch in a full implementation
    }
}
