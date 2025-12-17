package com.learnfirebase.ecommerce.product.infrastructure.search;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnfirebase.ecommerce.common.application.pagination.PageRequest;
import com.learnfirebase.ecommerce.product.application.dto.ProductSearchQuery;
import com.learnfirebase.ecommerce.product.application.dto.ProductSearchResult;
import com.learnfirebase.ecommerce.product.application.port.out.ProductSearchIndexPort;
import com.learnfirebase.ecommerce.product.application.port.out.ProductSearchPort;
import com.learnfirebase.ecommerce.product.domain.model.Category;
import com.learnfirebase.ecommerce.product.domain.model.Product;
import com.learnfirebase.ecommerce.product.domain.model.ProductId;
import com.learnfirebase.ecommerce.common.domain.valueobject.Money;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchProductSearchAdapter implements ProductSearchIndexPort, ProductSearchPort {

    private final RestClient restClient;
    private final ElasticsearchProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void index(Product product) {
        try {
            Map<String, Object> doc = new LinkedHashMap<>();
            doc.put("id", product.getId().getValue());
            doc.put("name", product.getName());
            doc.put("description", product.getDescription());
            doc.put("price", product.getPrice().getAmount());
            doc.put("currency", product.getPrice().getCurrency());
            doc.put("quantity", product.getStock());
            doc.put("soldCount", product.getSoldCount() == null ? 0 : product.getSoldCount());
            doc.put("sellerId", product.getSellerId());
            doc.put("categoryId", product.getCategory() != null ? product.getCategory().getId() : null);
            doc.put("createdAt", product.getCreatedAt() != null ? product.getCreatedAt().toString() : Instant.now().toString());
            doc.put("updatedAt", product.getUpdatedAt() != null ? product.getUpdatedAt().toString() : Instant.now().toString());

            Request request = new Request("PUT", "/" + properties.getIndex() + "/_doc/" + product.getId().getValue());
            request.addParameter("refresh", "wait_for");
            request.setJsonEntity(objectMapper.writeValueAsString(doc));
            restClient.performRequest(request);
        } catch (Exception e) {
            log.warn("Failed to index product {}: {}", product.getId().getValue(), e.getMessage());
        }
    }

    @Override
    public ProductSearchResult search(ProductSearchQuery query, PageRequest pageRequest) {
        int size = pageRequest.getSize() > 0 ? pageRequest.getSize() : (query.getSize() != null ? query.getSize() : 10);
        int from = Math.max(pageRequest.getPage(), 0) * size;

        Map<String, Object> body = new HashMap<>();
        Map<String, Object> bool = new HashMap<>();
        List<Object> must = new ArrayList<>();
        List<Object> filters = new ArrayList<>();

        if (query.getSearch() != null && !query.getSearch().isBlank()) {
            Map<String, Object> multiMatch = Map.of(
                "multi_match", Map.of(
                    "query", query.getSearch(),
                    "fields", List.of("name^3", "description"),
                    "type", "best_fields"
                )
            );
            must.add(multiMatch);
        }

        if (query.getCategory() != null && !query.getCategory().isBlank()) {
            filters.add(Map.of("term", Map.of("categoryId.keyword", query.getCategory())));
        }

        Map<String, Object> priceRange = new HashMap<>();
        if (query.getMinPrice() != null) {
            priceRange.put("gte", query.getMinPrice());
        }
        if (query.getMaxPrice() != null) {
            priceRange.put("lte", query.getMaxPrice());
        }
        if (!priceRange.isEmpty()) {
            filters.add(Map.of("range", Map.of("price", priceRange)));
        }

        bool.put("must", must);
        bool.put("filter", filters);

        body.put("query", Map.of("bool", bool));
        body.put("from", from);
        body.put("size", size);

        String sortField = "createdAt";
        String sortDir = "desc";
        if (query.getSort() != null && !query.getSort().isBlank()) {
            String[] parts = query.getSort().split(",");
            sortField = parts[0];
            if (parts.length > 1) {
                sortDir = parts[1];
            }
        } else if (pageRequest.getSort() != null && !pageRequest.getSort().isBlank()) {
            String[] parts = pageRequest.getSort().split(",");
            sortField = parts[0];
            if (parts.length > 1) {
                sortDir = parts[1];
            }
        }
        body.put("sort", List.of(Map.of(sortField, Map.of("order", sortDir))));

        body.put("aggs", Map.of(
            "by_category", Map.of(
                "terms", Map.of("field", "categoryId.keyword", "size", 20)
            )
        ));

        try {
            Request req = new Request("POST", "/" + properties.getIndex() + "/_search");
            req.setJsonEntity(objectMapper.writeValueAsString(body));
            Response response = restClient.performRequest(req);
            JsonNode root = objectMapper.readTree(response.getEntity().getContent());
            JsonNode hitsNode = root.path("hits").path("hits");
            List<Product> products = new ArrayList<>();
            hitsNode.forEach(hit -> {
                Product p = toProduct(hit.path("_source"));
                if (p != null) products.add(p);
            });
            long total = root.path("hits").path("total").path("value").asLong(products.size());
            Map<String, Long> facets = new LinkedHashMap<>();
            JsonNode buckets = root.path("aggregations").path("by_category").path("buckets");
            buckets.forEach(b -> facets.put(b.path("key").asText(), b.path("doc_count").asLong()));

            return ProductSearchResult.builder()
                .products(products)
                .totalElements(total)
                .totalPages((int) Math.ceil((double) total / size))
                .page(pageRequest.getPage())
                .size(size)
                .categoryFacets(facets)
                .suggestions(Collections.emptyList())
                .build();
        } catch (IOException e) {
            log.warn("Elasticsearch search failed: {}", e.getMessage());
            throw new IllegalStateException("Search backend unavailable", e);
        }
    }

    @Override
    public List<String> suggest(String prefix, int limit) {
        if (prefix == null || prefix.isBlank()) return Collections.emptyList();
        Map<String, Object> body = Map.of(
            "size", limit,
            "query", Map.of(
                "match_phrase_prefix", Map.of("name", Map.of("query", prefix))
            )
        );
        try {
            Request req = new Request("POST", "/" + properties.getIndex() + "/_search");
            req.setJsonEntity(objectMapper.writeValueAsString(body));
            Response resp = restClient.performRequest(req);
            JsonNode hits = objectMapper.readTree(resp.getEntity().getContent()).path("hits").path("hits");
            List<String> suggestions = new ArrayList<>();
            hits.forEach(hit -> {
                String name = hit.path("_source").path("name").asText(null);
                if (name != null) suggestions.add(name);
            });
            return suggestions.stream().distinct().limit(limit).toList();
        } catch (Exception e) {
            log.warn("Suggest query failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<Product> similarProducts(String productId, int limit) {
        if (productId == null) return Collections.emptyList();
        Map<String, Object> body = Map.of(
            "size", limit,
            "query", Map.of(
                "more_like_this", Map.of(
                    "fields", List.of("name", "description"),
                    "like", List.of(Map.of("_id", productId)),
                    "min_term_freq", 1,
                    "min_doc_freq", 1
                )
            )
        );
        try {
            Request req = new Request("POST", "/" + properties.getIndex() + "/_search");
            req.setJsonEntity(objectMapper.writeValueAsString(body));
            Response resp = restClient.performRequest(req);
            JsonNode hits = objectMapper.readTree(resp.getEntity().getContent()).path("hits").path("hits");
            List<Product> products = new ArrayList<>();
            hits.forEach(hit -> {
                Product p = toProduct(hit.path("_source"));
                if (p != null && !p.getId().getValue().equals(productId)) {
                    products.add(p);
                }
            });
            return products.stream().limit(limit).toList();
        } catch (Exception e) {
            log.warn("Similar products query failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private Product toProduct(JsonNode source) {
        try {
            String id = source.path("id").asText(null);
            if (id == null) {
                // fallback to doc id field may be missing; try productId
                id = source.path("productId").asText(null);
            }
            if (id == null) return null;
            BigDecimal price = source.path("price").isNumber()
                ? source.path("price").decimalValue()
                : new BigDecimal(source.path("price").asText("0"));
            String currency = source.path("currency").asText("VND");
            Integer quantity = source.path("quantity").isNumber() ? source.path("quantity").asInt() : null;
            Integer sold = source.path("soldCount").isNumber() ? source.path("soldCount").asInt() : null;
            String categoryId = source.path("categoryId").asText(null);

            return Product.builder()
                .id(new ProductId(id))
                .name(source.path("name").asText())
                .description(source.path("description").asText(null))
                .price(Money.builder().amount(price).currency(currency).build())
                .stock(quantity)
                .soldCount(sold)
                .sellerId(source.path("sellerId").asText(null))
                .category(categoryId != null ? Category.builder().id(categoryId).name(categoryId).build() : null)
                .createdAt(parseInstant(source.path("createdAt").asText(null)))
                .updatedAt(parseInstant(source.path("updatedAt").asText(null)))
                .build();
        } catch (Exception e) {
            log.warn("Failed to map search hit to product: {}", e.getMessage());
            return null;
        }
    }

    private Instant parseInstant(String raw) {
        if (raw == null) return null;
        try {
            return Instant.parse(raw);
        } catch (Exception e) {
            return null;
        }
    }
}
