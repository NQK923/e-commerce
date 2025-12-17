export type OrderItem = {
  id: string;
  productId: string;
  variantSku?: string;
  flashSaleId?: string;
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

export type Address = {
  fullName: string;
  phoneNumber?: string;
  line1: string;
  line2?: string;
  city: string;
  state?: string;
  postalCode: string;
  country: string;
};

export type CreateOrderRequest = {
  userId?: string;
  currency?: string;
  address?: Address;
  items: Array<{
    productId: string;
    variantSku?: string;
    flashSaleId?: string;
    quantity: number;
    price: number;
  }>;
};
