package com.learnfirebase.ecommerce.logistics.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.learnfirebase.ecommerce.logistics.application.port.out.LogisticsEventPublisher;
import com.learnfirebase.ecommerce.logistics.application.port.out.ShippingPartnerGateway;
import com.learnfirebase.ecommerce.logistics.application.port.out.ShippingRateRepository;
import com.learnfirebase.ecommerce.logistics.application.service.LogisticsApplicationService;
import com.learnfirebase.ecommerce.logistics.infrastructure.persistence.ShippingRateEntity;
import com.learnfirebase.ecommerce.logistics.infrastructure.persistence.ShippingRateJpaRepository;

@Configuration
@EnableJpaRepositories(basePackageClasses = ShippingRateJpaRepository.class)
@EntityScan(basePackageClasses = ShippingRateEntity.class)
public class LogisticsModuleConfig {
    @Bean
    public LogisticsApplicationService logisticsApplicationService(ShippingRateRepository shippingRateRepository, ShippingPartnerGateway shippingPartnerGateway, LogisticsEventPublisher logisticsEventPublisher) {
        return new LogisticsApplicationService(shippingRateRepository, shippingPartnerGateway, logisticsEventPublisher);
    }
}
