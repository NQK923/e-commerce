import { apiRequest } from "../lib/api-client";
import { buildQueryString } from "../lib/query-string";
import { PaginatedResponse } from "../types/common";
import { Product, ProductListParams } from "../types/product";

type BackendPageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

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
    return apiRequest<BackendPageResponse<Product>>(`/api/products${query}`).then(
      (resp): PaginatedResponse<Product> => ({
        items: resp?.content ?? [],
        page: resp?.page ?? 0,
        size: resp?.size ?? 0,
        total: resp?.totalElements ?? resp?.content?.length ?? 0,
        totalPages: resp?.totalPages,
      }),
    );
  },
  detail: (id: string) => apiRequest<Product>(`/api/products/${id}`),
};
