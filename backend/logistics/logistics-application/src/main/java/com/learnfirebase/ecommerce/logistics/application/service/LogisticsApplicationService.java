package com.learnfirebase.ecommerce.logistics.application.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.learnfirebase.ecommerce.common.domain.valueobject.Money;
import com.learnfirebase.ecommerce.logistics.application.command.CreateShipmentCommand;
import com.learnfirebase.ecommerce.logistics.application.dto.ShipmentDto;
import com.learnfirebase.ecommerce.logistics.application.port.in.CreateShipmentUseCase;
import com.learnfirebase.ecommerce.logistics.application.port.out.LogisticsEventPublisher;
import com.learnfirebase.ecommerce.logistics.application.port.out.ShippingPartnerGateway;
import com.learnfirebase.ecommerce.logistics.application.port.out.ShippingRateRepository;
import com.learnfirebase.ecommerce.logistics.domain.model.DeliveryEstimate;
import com.learnfirebase.ecommerce.logistics.domain.model.Shipment;
import com.learnfirebase.ecommerce.logistics.domain.model.ShippingMethod;
import com.learnfirebase.ecommerce.logistics.domain.model.ShippingRate;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LogisticsApplicationService implements CreateShipmentUseCase {
    private final ShippingRateRepository shippingRateRepository;
    private final ShippingPartnerGateway shippingPartnerGateway;
    private final LogisticsEventPublisher logisticsEventPublisher;

    @Override
    public ShipmentDto execute(CreateShipmentCommand command) {
        ShippingRate rate = shippingRateRepository.findByMethodAndDestination(command.getShippingMethodId(), command.getDestination())
            .orElse(ShippingRate.builder()
                .methodId(command.getShippingMethodId())
                .destination(command.getDestination())
                .cost(Money.builder().amount(BigDecimal.valueOf(10)).currency("USD").build())
                .build());
        Shipment shipment = Shipment.builder()
            .id(UUID.randomUUID().toString())
            .orderId(command.getOrderId())
            .method(ShippingMethod.builder().id(command.getShippingMethodId()).name(command.getShippingMethodId()).build())
            .estimate(DeliveryEstimate.builder().estimatedDelivery(Instant.now().plusSeconds(86400)).build())
            .shippedAt(Instant.now())
            .build();
        shippingPartnerGateway.dispatch(shipment);
        logisticsEventPublisher.publish(new com.learnfirebase.ecommerce.common.domain.DomainEvent() {});
        return ShipmentDto.builder()
            .id(shipment.getId())
            .orderId(shipment.getOrderId())
            .methodId(shipment.getMethod().getId())
            .cost(rate.getCost().getAmount().toPlainString())
            .currency(rate.getCost().getCurrency())
            .shippedAt(shipment.getShippedAt())
            .build();
    }
}
