import { apiRequest } from "../lib/api-client";
import { buildQueryString } from "../lib/query-string";
import { PaginatedResponse } from "../types/common";
import { Product, ProductListParams, UpsertProductRequest } from "../types/product";
import { ProductImage } from "../types/product";

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
  create: (payload: UpsertProductRequest) =>
    apiRequest<Product>("/api/products", {
      method: "POST",
      body: {
        name: payload.name,
        description: payload.description,
        price: payload.price.toString(),
        currency: payload.currency ?? "VND",
        categoryId: payload.categoryId,
        images: payload.images?.map((img: ProductImage, idx: number) => ({
          url: img.url,
          sortOrder: idx,
          primaryImage: img.primary ?? idx === 0,
        })),
      },
    }),
};
