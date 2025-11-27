package com.learnfirebase.ecommerce.report.domain.model;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DailySalesReport {
    LocalDate date;
    double totalSales;
    int totalOrders;
}
