package com.learnfirebase.ecommerce.report.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.learnfirebase.ecommerce.report.application.port.out.RawEventReaderPort;
import com.learnfirebase.ecommerce.report.application.port.out.ReportRepository;
import com.learnfirebase.ecommerce.report.application.service.ReportApplicationService;

@Configuration
public class ReportModuleConfig {
    @Bean
    public ReportApplicationService reportApplicationService(RawEventReaderPort rawEventReaderPort, ReportRepository reportRepository) {
        return new ReportApplicationService(rawEventReaderPort, reportRepository);
    }
}
