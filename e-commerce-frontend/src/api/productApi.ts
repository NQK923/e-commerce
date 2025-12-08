import { apiRequest } from "../lib/api-client";
import { buildQueryString } from "../lib/query-string";
import { PaginatedResponse } from "../types/common";
import { Product, ProductListParams, UpsertProductRequest } from "../types/product";

export type Review = {
  id: string;
  productId: string;
  userId: string;
  userName: string;
  rating: number;
  comment: string;
  createdAt: string;
};

export type CreateReviewRequest = {
  userId: string;
  userName: string;
  rating: number;
  comment: string;
};

type BackendPageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

type BackendProductDto = {
  id: string;
  name: string;
  description: string;
  price: string;
  currency?: string;
  sku?: string;
  quantity?: number;
  rating?: number;
  categoryId?: string;
  flashSaleStartAt?: string;
  flashSaleEndAt?: string;
  discountPercentage?: number;
  images?: Array<{
    id?: string;
    url: string;
    altText?: string;
    primaryImage?: boolean;
  }>;
};

const mapProduct = (dto: BackendProductDto): Product => {
  const price = Number.parseFloat(dto.price ?? "0");
  return {
    id: dto.id,
    name: dto.name,
    description: dto.description,
    price: Number.isFinite(price) ? price : 0,
    currency: dto.currency ?? "VND",
    sku: dto.sku,
    stock: dto.quantity,
    rating: dto.rating,
    category: dto.categoryId,
    flashSaleStartAt: dto.flashSaleStartAt,
    flashSaleEndAt: dto.flashSaleEndAt,
    discountPercentage: dto.discountPercentage,
    images:
      dto.images?.map((img, idx) => ({
        id: img.id ?? `${dto.id}-img-${idx}`,
        url: img.url,
        altText: img.altText,
        primary: img.primaryImage ?? idx === 0,
      })) ?? [],
  };
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
    return apiRequest<BackendPageResponse<BackendProductDto>>(`/api/products${query}`).then(
      (resp): PaginatedResponse<Product> => ({
        items: (resp?.content ?? []).map(mapProduct),
        page: resp?.page ?? 0,
        size: resp?.size ?? 0,
        total: resp?.totalElements ?? resp?.content?.length ?? 0,
        totalPages: resp?.totalPages,
      }),
    );
  },
  detail: (id: string) => apiRequest<BackendProductDto>(`/api/products/${id}`).then(mapProduct),
  create: (payload: UpsertProductRequest) =>
    apiRequest<BackendProductDto>("/api/products", {
      method: "POST",
      body: {
        name: payload.name,
        description: payload.description,
        price: payload.price.toString(),
        currency: payload.currency ?? "VND",
        quantity: payload.quantity ?? 0,
        categoryId: payload.categoryId,
        variants: payload.variants?.map((v) => ({
          sku: v.sku,
          name: v.name,
          price: v.price.toString(),
          quantity: v.quantity,
        })),
        images: payload.images?.map((img, idx: number) => ({
          url: img.url,
          sortOrder: idx,
          primaryImage: img.primary ?? idx === 0,
        })),
      },
    }).then(mapProduct),
  update: (id: string, payload: UpsertProductRequest) =>
    apiRequest<BackendProductDto>(`/api/products/${id}`, {
      method: "POST",
      body: {
        name: payload.name,
        description: payload.description,
        price: payload.price.toString(),
        currency: payload.currency ?? "VND",
        quantity: payload.quantity ?? 0,
        categoryId: payload.categoryId,
        variants: payload.variants?.map((v) => ({
          sku: v.sku,
          name: v.name,
          price: v.price.toString(),
          quantity: v.quantity,
        })),
        images: payload.images?.map((img, idx: number) => ({
          url: img.url,
          sortOrder: idx,
          primaryImage: img.primary ?? idx === 0,
        })),
      },
    }).then(mapProduct),
  
  fetchReviews: (productId: string, page = 0, size = 10) =>
    apiRequest<BackendPageResponse<Review>>(`/api/products/${productId}/reviews?page=${page}&size=${size}`).then(
      (resp): PaginatedResponse<Review> => ({
        items: resp?.content ?? [],
        page: resp?.page ?? 0,
        size: resp?.size ?? 0,
        total: resp?.totalElements ?? 0,
        totalPages: resp?.totalPages ?? 0,
      })
    ),

  addReview: (productId: string, payload: CreateReviewRequest) =>
    apiRequest<Review>(`/api/products/${productId}/reviews`, {
      method: "POST",
      body: payload,
    }),
};
