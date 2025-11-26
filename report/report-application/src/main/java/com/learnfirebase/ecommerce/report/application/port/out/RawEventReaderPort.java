package com.learnfirebase.ecommerce.report.application.port.out;

public interface RawEventReaderPort {
    double totalRevenueForDay(java.time.LocalDate date);

    int totalOrdersForDay(java.time.LocalDate date);
}
