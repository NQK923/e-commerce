package com.learnfirebase.ecommerce.product.application.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.learnfirebase.ecommerce.product.application.command.CreateProductReportCommand;
import com.learnfirebase.ecommerce.product.application.dto.ProductReportDto;
import com.learnfirebase.ecommerce.product.application.port.in.ManageProductReportUseCase;
import com.learnfirebase.ecommerce.product.application.port.out.ProductReportRepository;
import com.learnfirebase.ecommerce.product.domain.model.ProductId;
import com.learnfirebase.ecommerce.product.domain.model.ProductReport;
import com.learnfirebase.ecommerce.product.domain.model.ProductReportId;
import com.learnfirebase.ecommerce.product.domain.model.ReportStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductReportApplicationService implements ManageProductReportUseCase {

    private final ProductReportRepository reportRepository;

    @Override
    @Transactional
    public void createReport(CreateProductReportCommand command) {
        ProductReport report = ProductReport.builder()
                .id(new ProductReportId(UUID.randomUUID().toString()))
                .productId(new ProductId(command.getProductId()))
                .userId(command.getUserId())
                .reason(command.getReason())
                .description(command.getDescription())
                .status(ReportStatus.PENDING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        reportRepository.save(report);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductReportDto> listReports() {
        return reportRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void resolveReport(String reportId) {
        ProductReport report = reportRepository.findById(new ProductReportId(reportId))
                .orElseThrow(() -> new RuntimeException("Report not found"));
        report.resolve();
        reportRepository.save(report);
    }

    @Override
    @Transactional
    public void rejectReport(String reportId) {
        ProductReport report = reportRepository.findById(new ProductReportId(reportId))
                .orElseThrow(() -> new RuntimeException("Report not found"));
        report.reject();
        reportRepository.save(report);
    }

    private ProductReportDto mapToDto(ProductReport report) {
        return ProductReportDto.builder()
                .id(report.getId().getValue())
                .productId(report.getProductId().getValue())
                .userId(report.getUserId())
                .reason(report.getReason())
                .description(report.getDescription())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
