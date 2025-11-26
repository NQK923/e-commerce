package com.learnfirebase.ecommerce.report.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RevenueAggregation {
    double totalRevenue;
    double averageOrderValue;
}
