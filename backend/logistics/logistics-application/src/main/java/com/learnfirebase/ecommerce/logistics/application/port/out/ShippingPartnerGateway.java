package com.learnfirebase.ecommerce.logistics.application.port.out;

import com.learnfirebase.ecommerce.logistics.domain.model.Shipment;

public interface ShippingPartnerGateway {
    void dispatch(Shipment shipment);
}
