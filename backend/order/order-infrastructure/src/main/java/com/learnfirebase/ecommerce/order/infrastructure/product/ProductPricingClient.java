package com.learnfirebase.ecommerce.order.infrastructure.product;

import java.util.Collections;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.learnfirebase.ecommerce.order.application.port.out.LoadProductPort;

@Component
public class ProductPricingClient implements LoadProductPort {
    @Override
    public Map<String, String> loadProductPrices(String currency, Iterable<String> productIds) {
        // Integrate with product service or search index for real data
        return Collections.emptyMap();
    }
}
