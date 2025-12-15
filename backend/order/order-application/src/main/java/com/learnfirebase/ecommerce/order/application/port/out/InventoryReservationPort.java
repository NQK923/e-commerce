package com.learnfirebase.ecommerce.order.application.port.out;

import java.util.Map;

public interface InventoryReservationPort {
    boolean reserve(String orderId, Map<String, Integer> productQuantities);
    boolean reserveFlashSale(String orderId, String flashSaleId, int quantity);
}
