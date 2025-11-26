package com.learnfirebase.ecommerce.common.application.pagination;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PageRequest {
    int page;
    int size;
    String sort;
}
