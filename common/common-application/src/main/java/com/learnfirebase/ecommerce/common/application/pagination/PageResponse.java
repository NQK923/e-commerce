package com.learnfirebase.ecommerce.common.application.pagination;

import java.util.List;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
public class PageResponse<T> {
    @Singular("item")
    List<T> content;
    long totalElements;
    int totalPages;
    int page;
    int size;
}
