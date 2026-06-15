package com.learnfirebase.ecommerce.product.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.learnfirebase.ecommerce.product.application.command.CreateProductReportCommand;
import com.learnfirebase.ecommerce.product.application.port.out.ProductReportRepository;
import com.learnfirebase.ecommerce.product.domain.exception.ProductDomainException;
import com.learnfirebase.ecommerce.product.domain.model.ProductId;
import com.learnfirebase.ecommerce.product.domain.model.ProductReport;
import com.learnfirebase.ecommerce.product.domain.model.ProductReportId;
import com.learnfirebase.ecommerce.product.domain.model.ReportReason;
import com.learnfirebase.ecommerce.product.domain.model.ReportStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductReportApplicationServiceTest {
    @Mock
    private ProductReportRepository reportRepository;

    private ProductReportApplicationService service;

    @BeforeEach
    void setUp() {
        service = new ProductReportApplicationService(reportRepository);
    }

    @Test
    void createReportPersistsPendingReport() {
        service.createReport(CreateProductReportCommand.builder()
            .productId("product-1")
            .userId("buyer-1")
            .reason(ReportReason.SCAM)
            .description("Suspicious listing")
            .build());

        ArgumentCaptor<ProductReport> reportCaptor = ArgumentCaptor.forClass(ProductReport.class);
        verify(reportRepository).save(reportCaptor.capture());
        ProductReport report = reportCaptor.getValue();
        assertThat(report.getId()).isNotNull();
        assertThat(report.getProductId()).isEqualTo(new ProductId("product-1"));
        assertThat(report.getUserId()).isEqualTo("buyer-1");
        assertThat(report.getReason()).isEqualTo(ReportReason.SCAM);
        assertThat(report.getStatus()).isEqualTo(ReportStatus.PENDING);
        assertThat(report.getCreatedAt()).isNotNull();
    }

    @Test
    void listReportsMapsDomainToDto() {
        when(reportRepository.findAll()).thenReturn(List.of(report("report-1", ReportStatus.PENDING)));

        var reports = service.listReports();

        assertThat(reports).singleElement()
            .satisfies(report -> {
                assertThat(report.getId()).isEqualTo("report-1");
                assertThat(report.getProductId()).isEqualTo("product-1");
                assertThat(report.getUserId()).isEqualTo("buyer-1");
                assertThat(report.getReason()).isEqualTo(ReportReason.FAKE);
                assertThat(report.getStatus()).isEqualTo(ReportStatus.PENDING);
            });
    }

    @Test
    void resolveReportMarksReportResolvedAndSaves() {
        ProductReport report = report("report-1", ReportStatus.PENDING);
        when(reportRepository.findById(new ProductReportId("report-1"))).thenReturn(Optional.of(report));

        service.resolveReport("report-1");

        ArgumentCaptor<ProductReport> reportCaptor = ArgumentCaptor.forClass(ProductReport.class);
        verify(reportRepository).save(reportCaptor.capture());
        assertThat(reportCaptor.getValue().getStatus()).isEqualTo(ReportStatus.RESOLVED);
        assertThat(reportCaptor.getValue().getUpdatedAt()).isAfter(Instant.parse("2026-06-15T00:00:00Z"));
    }

    @Test
    void rejectReportMarksReportRejectedAndSaves() {
        ProductReport report = report("report-1", ReportStatus.PENDING);
        when(reportRepository.findById(new ProductReportId("report-1"))).thenReturn(Optional.of(report));

        service.rejectReport("report-1");

        ArgumentCaptor<ProductReport> reportCaptor = ArgumentCaptor.forClass(ProductReport.class);
        verify(reportRepository).save(reportCaptor.capture());
        assertThat(reportCaptor.getValue().getStatus()).isEqualTo(ReportStatus.REJECTED);
        assertThat(reportCaptor.getValue().getUpdatedAt()).isAfter(Instant.parse("2026-06-15T00:00:00Z"));
    }

    @Test
    void resolveMissingReportThrowsDomainExceptionAndDoesNotSave() {
        when(reportRepository.findById(new ProductReportId("missing"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resolveReport("missing"))
            .isInstanceOf(ProductDomainException.class)
            .hasMessage("Report not found");

        verify(reportRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectMissingReportThrowsDomainExceptionAndDoesNotSave() {
        when(reportRepository.findById(new ProductReportId("missing"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.rejectReport("missing"))
            .isInstanceOf(ProductDomainException.class)
            .hasMessage("Report not found");

        verify(reportRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private ProductReport report(String id, ReportStatus status) {
        return ProductReport.builder()
            .id(new ProductReportId(id))
            .productId(new ProductId("product-1"))
            .userId("buyer-1")
            .reason(ReportReason.FAKE)
            .description("Counterfeit item")
            .status(status)
            .createdAt(Instant.parse("2026-06-15T00:00:00Z"))
            .updatedAt(Instant.parse("2026-06-15T00:00:00Z"))
            .build();
    }
}
