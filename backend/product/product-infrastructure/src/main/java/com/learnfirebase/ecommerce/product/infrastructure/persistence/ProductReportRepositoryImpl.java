package com.learnfirebase.ecommerce.product.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.learnfirebase.ecommerce.product.application.port.out.ProductReportRepository;
import com.learnfirebase.ecommerce.product.domain.model.ProductReport;
import com.learnfirebase.ecommerce.product.domain.model.ProductReportId;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductReportRepositoryImpl implements ProductReportRepository {
    private final ProductReportJpaRepository jpaRepository;

    @Override
    public void save(ProductReport report) {
        jpaRepository.save(ProductReportEntity.fromDomain(report));
    }

    @Override
    public Optional<ProductReport> findById(ProductReportId id) {
        return jpaRepository.findById(id.getValue()).map(ProductReportEntity::toDomain);
    }

    @Override
    public List<ProductReport> findAll() {
        return jpaRepository.findAll().stream()
                .map(ProductReportEntity::toDomain)
                .collect(Collectors.toList());
    }
}
