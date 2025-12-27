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
  trackingNumber?: string;
  trackingCarrier?: string;
  shippedAt?: string;
  deliveredAt?: string;
  returnStatus?: string;
  returnReason?: string;
  returnNote?: string;
  returnRequestedAt?: string;
  returnResolvedAt?: string;
  refundAmount?: string;
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
  trackingNumber: dto.trackingNumber,
  trackingCarrier: dto.trackingCarrier,
  shippedAt: dto.shippedAt,
  deliveredAt: dto.deliveredAt,
  returnStatus: dto.returnStatus,
  returnReason: dto.returnReason,
  returnNote: dto.returnNote,
  returnRequestedAt: dto.returnRequestedAt,
  returnResolvedAt: dto.returnResolvedAt,
  refundAmount: dto.refundAmount ? parseFloat(dto.refundAmount) : undefined,
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
  ship: (orderId: string, payload: { trackingNumber: string; trackingCarrier?: string }) =>
    apiRequest<BackendOrder>(`/api/orders/${orderId}/ship`, { method: "POST", body: payload }).then(mapOrder),
  markDelivered: (orderId: string) =>
    apiRequest<BackendOrder>(`/api/orders/${orderId}/deliver`, { method: "POST" }).then(mapOrder),
  requestReturn: (orderId: string, payload: { userId?: string; reason?: string; note?: string }) =>
    apiRequest<BackendOrder>(`/api/orders/${orderId}/return`, { method: "POST", body: payload }).then(mapOrder),
  approveReturn: (orderId: string, payload: { refundAmount?: string; currency?: string; note?: string }) =>
    apiRequest<BackendOrder>(`/api/orders/${orderId}/returns/approve`, { method: "POST", body: payload }).then(mapOrder),
  rejectReturn: (orderId: string, payload: { note?: string }) =>
    apiRequest<BackendOrder>(`/api/orders/${orderId}/returns/reject`, { method: "POST", body: payload }).then(mapOrder),

  initiatePayment: (orderId: string, payload: { returnUrl: string }) =>
    apiRequest<PaymentInitResponse>(`/api/orders/${orderId}/payment/vnpay`, { method: "POST", body: payload }),

  verifyPayment: (queryString: string) =>
    apiRequest<BackendOrder>(`/api/payments/vnpay/return?${queryString}`).then(mapOrder),
};

type PaymentInitResponse = {
  paymentUrl: string;
  reference: string;
};
