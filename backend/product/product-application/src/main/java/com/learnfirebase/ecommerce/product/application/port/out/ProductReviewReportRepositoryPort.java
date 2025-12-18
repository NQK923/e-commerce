package com.learnfirebase.ecommerce.product.application.port.out;

import com.learnfirebase.ecommerce.product.application.command.ReportReviewCommand;

public interface ProductReviewReportRepositoryPort {
    void saveReport(ReportReviewCommand command);

    boolean hasReported(String reviewId, String reporterUserId);
}
