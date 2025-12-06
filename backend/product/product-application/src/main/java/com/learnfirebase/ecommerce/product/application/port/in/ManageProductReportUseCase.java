package com.learnfirebase.ecommerce.product.application.port.in;

import java.util.List;
import com.learnfirebase.ecommerce.product.application.command.CreateProductReportCommand;
import com.learnfirebase.ecommerce.product.application.dto.ProductReportDto;

public interface ManageProductReportUseCase {
    void createReport(CreateProductReportCommand command);
    List<ProductReportDto> listReports();
    void resolveReport(String reportId);
    void rejectReport(String reportId);
}
