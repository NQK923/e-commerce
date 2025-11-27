import { apiRequest } from "../lib/api-client";
import { buildQueryString } from "../lib/query-string";
import { PaginatedResponse } from "../types/common";
import { Product, ProductListParams } from "../types/product";

export const productApi = {
  list: (params: ProductListParams = {}) => {
    const query = buildQueryString({
      search: params.search,
      category: params.category,
      minPrice: params.minPrice,
      maxPrice: params.maxPrice,
      page: params.page,
      size: params.size,
      sort: params.sort,
    });
    return apiRequest<PaginatedResponse<Product>>(`/api/products${query}`);
  },
  detail: (id: string) => apiRequest<Product>(`/api/products/${id}`),
};
