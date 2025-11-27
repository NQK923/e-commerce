import { apiRequest } from "../lib/api-client";
import { PaginatedResponse } from "../types/common";
import { CreateOrderRequest, Order } from "../types/order";

export const orderApi = {
  create: (payload: CreateOrderRequest) =>
    apiRequest<Order>("/api/orders", { method: "POST", body: payload }),
  list: (page = 0, size = 10) =>
    apiRequest<PaginatedResponse<Order>>(`/api/orders?page=${page}&size=${size}`),
  detail: (id: string) => apiRequest<Order>(`/api/orders/${id}`),
};
