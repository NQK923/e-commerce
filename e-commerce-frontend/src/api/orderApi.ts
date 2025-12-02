import { apiRequest } from "../lib/api-client";
import { PaginatedResponse } from "../types/common";
import { CreateOrderRequest, Order } from "../types/order";

type BackendPageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export const orderApi = {
  create: (payload: CreateOrderRequest) =>
    apiRequest<Order>("/api/orders", { method: "POST", body: payload }),
  list: (page = 0, size = 10) =>
    apiRequest<BackendPageResponse<Order>>(`/api/orders?page=${page}&size=${size}`).then(
      (resp): PaginatedResponse<Order> => ({
        items: resp?.content ?? [],
        page: resp?.page ?? page,
        size: resp?.size ?? size,
        total: resp?.totalElements ?? resp?.content?.length ?? 0,
        totalPages: resp?.totalPages,
      }),
    ),
  detail: (id: string) => apiRequest<Order>(`/api/orders/${id}`),
};
