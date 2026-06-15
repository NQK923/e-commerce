package com.learnfirebase.ecommerce.product.infrastructure.search;

import static org.assertj.core.api.Assertions.assertThat;

import com.learnfirebase.ecommerce.common.application.pagination.PageRequest;
import com.learnfirebase.ecommerce.product.application.dto.ProductSearchQuery;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.elasticsearch.client.RestClient;
import org.apache.http.HttpHost;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ElasticsearchProductSearchAdapterTest {

    private HttpServer server;
    private RestClient restClient;
    private ElasticsearchProductSearchAdapter adapter;
    private final List<String> requestBodies = new ArrayList<>();

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/products/_search", this::handleSearch);
        server.start();

        ElasticsearchProperties properties = new ElasticsearchProperties();
        properties.setHost("http://127.0.0.1:" + server.getAddress().getPort());
        properties.setIndex("products");
        restClient = RestClient.builder(HttpHost.create(properties.getHost())).build();
        adapter = new ElasticsearchProductSearchAdapter(restClient, properties);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (restClient != null) {
            restClient.close();
        }
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void searchMapsHitsAndFacetsFromElasticsearchResponse() {
        var result = adapter.search(
            ProductSearchQuery.builder()
                .search("phone")
                .category("electronics")
                .minPrice(java.math.BigDecimal.valueOf(100))
                .maxPrice(java.math.BigDecimal.valueOf(900))
                .sort("price,asc")
                .build(),
            PageRequest.builder().page(1).size(5).build()
        );

        assertThat(result.getTotalElements()).isEqualTo(7);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(5);
        assertThat(result.getCategoryFacets()).containsEntry("electronics", 7L);
        assertThat(result.getProducts()).hasSize(1);
        assertThat(result.getProducts().get(0).getId().getValue()).isEqualTo("product-1");
        assertThat(result.getProducts().get(0).getName()).isEqualTo("Smoke Phone");
        assertThat(result.getProducts().get(0).getPrice().getAmount()).isEqualByComparingTo("499.99");
        assertThat(result.getProducts().get(0).getCategory().getId()).isEqualTo("electronics");

        assertThat(requestBodies).hasSize(1);
        assertThat(requestBodies.get(0))
            .contains("\"from\":5")
            .contains("\"size\":5")
            .contains("\"multi_match\"")
            .contains("\"categoryId.keyword\"")
            .contains("\"price\"")
            .contains("\"order\":\"asc\"");
    }

    @Test
    void suggestReturnsDistinctProductNames() {
        var suggestions = adapter.suggest("pho", 5);

        assertThat(suggestions).containsExactly("Smoke Phone");
    }

    private void handleSearch(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        requestBodies.add(requestBody);
        String body = requestBody.contains("match_phrase_prefix") ? suggestResponse() : searchResponse();
        byte[] response = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    private String searchResponse() {
        return """
            {
              "hits": {
                "total": { "value": 7 },
                "hits": [
                  {
                    "_source": {
                      "id": "product-1",
                      "name": "Smoke Phone",
                      "description": "Fast phone",
                      "price": 499.99,
                      "currency": "VND",
                      "quantity": 12,
                      "soldCount": 3,
                      "sellerId": "seller-1",
                      "categoryId": "electronics",
                      "createdAt": "2026-06-15T00:00:00Z",
                      "updatedAt": "2026-06-15T01:00:00Z"
                    }
                  }
                ]
              },
              "aggregations": {
                "by_category": {
                  "buckets": [
                    { "key": "electronics", "doc_count": 7 }
                  ]
                }
              }
            }
            """;
    }

    private String suggestResponse() {
        return """
            {
              "hits": {
                "total": { "value": 2 },
                "hits": [
                  {
                    "_source": {
                      "id": "product-1",
                      "name": "Smoke Phone"
                    }
                  },
                  {
                    "_source": {
                      "id": "product-2",
                      "name": "Smoke Phone"
                    }
                  }
                ]
              }
            }
            """;
    }
}
