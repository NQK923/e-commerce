package com.learnfirebase.ecommerce.report.infrastructure.source;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.learnfirebase.ecommerce.report.application.port.out.RawEventReaderPort;

@Component
public class RawEventReaderAdapter implements RawEventReaderPort {
    @Override
    public double totalRevenueForDay(LocalDate date) {
        return 0.0;
    }

    @Override
    public int totalOrdersForDay(LocalDate date) {
        return 0;
    }
}
