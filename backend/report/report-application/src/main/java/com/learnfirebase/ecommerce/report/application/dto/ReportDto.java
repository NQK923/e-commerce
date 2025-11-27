package com.learnfirebase.ecommerce.report.application.dto;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ReportDto {
    LocalDate date;
    double totalRevenue;
    int totalOrders;
}
