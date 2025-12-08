import { apiRequest } from "../lib/api-client";
import { PaginatedResponse } from "../types/common";
import { CreateOrderRequest, Order, OrderItem } from "../types/order";

type BackendPageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

type BackendOrderDto = {
  id: string;
  userId: string;
  status: string;
  currency?: string;
  totalAmount?: string;
  createdAt: string;
  updatedAt?: string;
  items?: Array<{
    productId: string;
    quantity: number;
    price: string;
  }>;
};

const mapOrder = (dto: BackendOrderDto): Order => {
  const currency = dto.currency ?? "USD";
  const items: OrderItem[] = (dto.items ?? []).map((item, index) => {
    const price = Number(item.price ?? 0);
    return {
      id: `${dto.id}-${item.productId}-${index}`,
      productId: item.productId,
      quantity: item.quantity,
      price,
      subtotal: price * item.quantity,
    };
  });

  const computedTotal = items.reduce((sum, item) => sum + item.subtotal, 0);
  const totalAmount = Number(dto.totalAmount ?? computedTotal);

  return {
    id: dto.id,
    userId: dto.userId,
    status: dto.status,
    currency,
    createdAt: dto.createdAt,
    updatedAt: dto.updatedAt,
    total: totalAmount || computedTotal,
    items,
  };
};

export const orderApi = {
  create: (payload: CreateOrderRequest) =>
    apiRequest<BackendOrderDto>("/api/orders", {
      method: "POST",
      body: {
        userId: payload.userId,
        currency: payload.currency,
        items: payload.items?.map((item) => ({
          productId: item.productId,
          quantity: item.quantity,
          price: item.price.toString(),
        })),
      },
    }).then(mapOrder),
  list: (page = 0, size = 10) =>
    apiRequest<BackendPageResponse<BackendOrderDto>>(`/api/orders?page=${page}&size=${size}`).then(
      (resp): PaginatedResponse<Order> => ({
        items: (resp?.content ?? []).map(mapOrder),
        page: resp?.page ?? page,
        size: resp?.size ?? size,
        total: resp?.totalElements ?? resp?.content?.length ?? 0,
        totalPages: resp?.totalPages,
      }),
    ),
  detail: (id: string) => apiRequest<BackendOrderDto>(`/api/orders/${id}`).then(mapOrder),
};
