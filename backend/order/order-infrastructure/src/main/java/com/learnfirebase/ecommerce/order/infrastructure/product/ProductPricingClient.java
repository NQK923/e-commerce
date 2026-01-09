package com.learnfirebase.ecommerce.order.infrastructure.product;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.learnfirebase.ecommerce.order.application.port.out.LoadProductPort;
import com.learnfirebase.ecommerce.product.application.port.out.ProductRepository;
import com.learnfirebase.ecommerce.product.domain.model.ProductId;
import com.learnfirebase.ecommerce.product.domain.model.Product;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductPricingClient implements LoadProductPort {
    private final ProductRepository productRepository;

    @Override
    public Map<String, ProductInfo> loadProducts(String currency, Iterable<String> productIds) {
        Map<String, ProductInfo> result = new HashMap<>();
        for (String id : productIds) {
            Optional<Product> productOpt = productRepository.findById(new ProductId(id));
            if (productOpt.isPresent()) {
                Product p = productOpt.get();
                result.put(id, ProductInfo.builder()
                        .id(p.getId().getValue())
                        .price(p.getPrice().getAmount())
                        .currency(p.getPrice().getCurrency())
                        .sellerId(p.getSellerId())
                        .build());
            }
        }
        return result;
    }
}
