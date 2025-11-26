package com.learnfirebase.ecommerce.logistics.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.learnfirebase.ecommerce.logistics.application.port.out.LogisticsEventPublisher;
import com.learnfirebase.ecommerce.logistics.application.port.out.ShippingPartnerGateway;
import com.learnfirebase.ecommerce.logistics.application.port.out.ShippingRateRepository;
import com.learnfirebase.ecommerce.logistics.application.service.LogisticsApplicationService;

@Configuration
public class LogisticsModuleConfig {
    @Bean
    public LogisticsApplicationService logisticsApplicationService(ShippingRateRepository shippingRateRepository, ShippingPartnerGateway shippingPartnerGateway, LogisticsEventPublisher logisticsEventPublisher) {
        return new LogisticsApplicationService(shippingRateRepository, shippingPartnerGateway, logisticsEventPublisher);
    }
}
