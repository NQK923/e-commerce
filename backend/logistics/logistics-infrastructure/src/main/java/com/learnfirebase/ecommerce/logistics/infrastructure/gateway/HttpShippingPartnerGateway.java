package com.learnfirebase.ecommerce.logistics.infrastructure.gateway;
 
import org.springframework.stereotype.Component;
 
import com.learnfirebase.ecommerce.logistics.application.port.out.ShippingPartnerGateway;
import com.learnfirebase.ecommerce.logistics.domain.model.Shipment;
 
import lombok.extern.slf4j.Slf4j;
 
/**
 * Mock implementation of ShippingPartnerGateway for demo and local development.
 * Does not communicate with a real shipping provider API.
 */
@Slf4j
@Component
public class HttpShippingPartnerGateway implements ShippingPartnerGateway {
    @Override
    public void dispatch(Shipment shipment) {
        log.info("[DEMO LOGISTICS] Dispatching shipment {} to partner", shipment.getId());
    }
}
