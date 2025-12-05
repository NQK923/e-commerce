import { CartItem } from "./cart";

export type Order = {
  id: string;
  status: string;
  createdAt: string;
  total: number;
  currency?: string;
  items: CartItem[];
  shippingAddress?: Address;
  paymentStatus?: string;
};

export type Address = {
  fullName?: string;
  phoneNumber?: string;
  line1: string;
  line2?: string;
  city: string;
  state?: string;
  postalCode: string;
  country: string;
};

export type CreateOrderRequest = {
  address: Address;
};
