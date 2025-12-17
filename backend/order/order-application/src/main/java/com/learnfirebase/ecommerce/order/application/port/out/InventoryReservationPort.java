package com.learnfirebase.ecommerce.order.application.port.out;

import java.util.Map;

public interface InventoryReservationPort {
    boolean reserve(String orderId, Map<String, Integer> productQuantities);
    boolean reserveFlashSale(String orderId, String flashSaleId, int quantity);
    void release(String orderId, Map<String, Integer> productQuantities);
    void releaseFlashSale(String flashSaleId, int quantity);
    void confirm(String orderId);
    void releaseExpiredReservations();
}
