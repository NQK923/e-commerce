package com.learnfirebase.ecommerce.product.infrastructure.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.learnfirebase.ecommerce.common.application.pagination.PageRequest;
import com.learnfirebase.ecommerce.common.application.pagination.PageResponse;
import com.learnfirebase.ecommerce.product.application.dto.ProductSearchQuery;
import com.learnfirebase.ecommerce.product.application.dto.ProductSearchResult;
import com.learnfirebase.ecommerce.product.application.port.out.ProductRepository;
import com.learnfirebase.ecommerce.product.application.port.out.ProductSearchIndexPort;
import com.learnfirebase.ecommerce.product.application.port.out.ProductSearchPort;
import com.learnfirebase.ecommerce.product.domain.model.Product;
import com.learnfirebase.ecommerce.product.domain.model.ProductId;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchProductSearchAdapter implements ProductSearchIndexPort, ProductSearchPort {

    private final ProductRepository productRepository;

    @Override
    public void index(Product product) {
        // TODO: Replace with real Elasticsearch indexing; current placeholder keeps compile-time contract.
        log.info("Indexing product {} (placeholder)", product.getId().getValue());
    }

    @Override
    public ProductSearchResult search(ProductSearchQuery query, PageRequest pageRequest) {
        PageResponse<Product> page = productRepository.search(query,
            PageRequest.builder()
                .page(pageRequest.getPage())
                .size(pageRequest.getSize() > 0 ? pageRequest.getSize() : (query.getSize() != null ? query.getSize() : 10))
                .sort(pageRequest.getSort() != null ? pageRequest.getSort() : query.getSort())
                .build());

        Map<String, Long> categoryFacets = page.getContent().stream()
            .filter(p -> p.getCategory() != null && p.getCategory().getId() != null)
            .collect(Collectors.groupingBy(p -> p.getCategory().getId(), Collectors.counting()));

        return ProductSearchResult.builder()
            .products(page.getContent())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .page(page.getPage())
            .size(page.getSize())
            .categoryFacets(categoryFacets)
            .suggestions(Collections.emptyList())
            .build();
    }

    @Override
    public List<String> suggest(String prefix, int limit) {
        if (prefix == null || prefix.isBlank()) {
            return Collections.emptyList();
        }
        PageResponse<Product> page = productRepository.search(
            ProductSearchQuery.builder().search(prefix).build(),
            PageRequest.builder().page(0).size(limit).build());
        return page.getContent().stream()
            .map(Product::getName)
            .distinct()
            .limit(limit)
            .toList();
    }

    @Override
    public List<Product> similarProducts(String productId, int limit) {
        if (productId == null) return Collections.emptyList();
        Product current = productRepository.findById(new ProductId(productId)).orElse(null);
        if (current == null || current.getCategory() == null) return Collections.emptyList();

        PageResponse<Product> page = productRepository.search(
            ProductSearchQuery.builder().category(current.getCategory().getId()).build(),
            PageRequest.builder().page(0).size(limit * 2).build());

        List<Product> filtered = new ArrayList<>();
        for (Product p : page.getContent()) {
            if (!p.getId().getValue().equals(productId)) {
                filtered.add(p);
            }
        }
        filtered.sort(Comparator.comparingLong(p -> p.getSoldCount() != null ? -p.getSoldCount() : 0));
        return filtered.stream().limit(limit).toList();
    }
}
