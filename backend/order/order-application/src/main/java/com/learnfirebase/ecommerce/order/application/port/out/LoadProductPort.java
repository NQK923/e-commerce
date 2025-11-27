package com.learnfirebase.ecommerce.order.application.port.out;

import java.util.Map;

public interface LoadProductPort {
    Map<String, String> loadProductPrices(String currency, Iterable<String> productIds);
}
