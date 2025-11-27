import { Product } from "./product";

export type CartItem = {
  id: string;
  product: Product;
  quantity: number;
  unitPrice: number;
  subtotal: number;
};

export type Cart = {
  id: string;
  items: CartItem[];
  subtotal: number;
  discountTotal?: number;
  shippingEstimate?: number;
  total: number;
  currency?: string;
};

export type AddToCartRequest = {
  productId: string;
  quantity: number;
};

export type UpdateCartItemRequest = {
  itemId: string;
  quantity: number;
};
