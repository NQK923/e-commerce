package com.learnfirebase.ecommerce.order.application.port.out;

import java.util.Map;

public interface InventoryReservationPort {
    boolean reserve(String orderId, Map<String, Integer> productQuantities);
}
