import { Product } from "./product";

export type CartItem = {
  id: string;
  product: Product;
  variantSku?: string;
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
  variantSku?: string;
  quantity: number;
  price?: number;
  currency?: string;
  cartId?: string;
};

export type UpdateCartItemRequest = {
  itemId: string;
  quantity: number;
  variantSku?: string;
};
