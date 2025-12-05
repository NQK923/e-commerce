export type OrderItem = {
  id: string;
  productId: string;
  quantity: number;
  price: number;
  subtotal: number;
};

export type Order = {
  id: string;
  userId: string;
  status: string;
  createdAt: string;
  updatedAt?: string;
  total: number;
  currency?: string;
  items: OrderItem[];
};

export type CreateOrderRequest = {
  // Extend when checkout flow is implemented.
  userId?: string;
  currency?: string;
};
