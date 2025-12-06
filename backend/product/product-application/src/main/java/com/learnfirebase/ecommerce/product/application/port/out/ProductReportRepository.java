package com.learnfirebase.ecommerce.product.application.port.out;

import java.util.List;
import java.util.Optional;
import com.learnfirebase.ecommerce.product.domain.model.ProductReport;
import com.learnfirebase.ecommerce.product.domain.model.ProductReportId;

public interface ProductReportRepository {
    void save(ProductReport report);
    Optional<ProductReport> findById(ProductReportId id);
    List<ProductReport> findAll();
}
