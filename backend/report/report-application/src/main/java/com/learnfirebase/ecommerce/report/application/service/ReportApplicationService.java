package com.learnfirebase.ecommerce.report.application.service;

import com.learnfirebase.ecommerce.report.application.command.GenerateReportCommand;
import com.learnfirebase.ecommerce.report.application.dto.ReportDto;
import com.learnfirebase.ecommerce.report.application.port.in.GenerateDailyReportUseCase;
import com.learnfirebase.ecommerce.report.application.port.out.RawEventReaderPort;
import com.learnfirebase.ecommerce.report.application.port.out.ReportRepository;
import com.learnfirebase.ecommerce.report.domain.model.DailySalesReport;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReportApplicationService implements GenerateDailyReportUseCase {
    private final RawEventReaderPort rawEventReaderPort;
    private final ReportRepository reportRepository;

    @Override
    public ReportDto execute(GenerateReportCommand command) {
        double revenue = rawEventReaderPort.totalRevenueForDay(command.getDate());
        int orders = rawEventReaderPort.totalOrdersForDay(command.getDate());
        DailySalesReport report = DailySalesReport.builder()
            .date(command.getDate())
            .totalSales(revenue)
            .totalOrders(orders)
            .build();
        reportRepository.save(report);
        return ReportDto.builder()
            .date(report.getDate())
            .totalRevenue(report.getTotalSales())
            .totalOrders(report.getTotalOrders())
            .build();
    }
}
