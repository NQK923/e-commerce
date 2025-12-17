import { apiRequest } from "../lib/api-client";
import { buildQueryString } from "../lib/query-string";
import { ApiListParams, PaginatedResponse } from "../types/common";
import { Order } from "../types/order";

type BackendOrderItem = {
  productId: string;
  variantSku?: string;
  flashSaleId?: string;
  quantity: number;
  price: string;
};

type BackendOrder = {
  id: string;
  userId: string;
  status: string;
  currency: string;
  totalAmount: string;
  createdAt: string;
  updatedAt: string;
  items: BackendOrderItem[];
};

type BackendPageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

const mapOrder = (dto: BackendOrder): Order => ({
  id: dto.id,
  userId: dto.userId,
  status: dto.status,
  currency: dto.currency,
  total: parseFloat(dto.totalAmount),
  createdAt: dto.createdAt,
  updatedAt: dto.updatedAt,
  items: dto.items.map(i => ({
    id: `${dto.id}-${i.variantSku || i.productId}`, // Generate a fake ID for item as backend doesn't send it in this DTO
    productId: i.productId,
    variantSku: i.variantSku,
    flashSaleId: i.flashSaleId,
    quantity: i.quantity,
    price: parseFloat(i.price),
    subtotal: parseFloat(i.price) * i.quantity
  }))
});

type CreateOrderItem = {
  productId: string;
  variantSku?: string;
  flashSaleId?: string;
  quantity: number;
  price: number;
};

type CreateOrderRequest = {
  userId?: string;
  items: CreateOrderItem[];
  currency: string;
  address: {
    fullName: string;
    line1: string;
    line2?: string;
    city: string;
    state: string;
    postalCode: string;
    country: string;
  };
  paymentMethod: string;
};

export const orderApi = {
  list: (params: ApiListParams = {}) => {
    const query = buildQueryString(params);
    return apiRequest<BackendPageResponse<BackendOrder>>(`/api/orders${query}`).then(
      (resp): PaginatedResponse<Order> => ({
        items: (resp?.content ?? []).map(mapOrder),
        page: resp?.page ?? 0,
        size: resp?.size ?? 0,
        total: resp?.totalElements ?? 0,
        totalPages: resp?.totalPages,
      })
    );
  },
  get: (id: string) => apiRequest<BackendOrder>(`/api/orders/${id}`).then(mapOrder),
  create: (data: CreateOrderRequest) => apiRequest<BackendOrder>("/api/orders", { method: "POST", body: data }).then(mapOrder),
};
