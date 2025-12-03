import { apiRequest } from "../lib/api-client";
import { AddToCartRequest, Cart, UpdateCartItemRequest } from "../types/cart";
import { Product } from "../types/product";

const toProduct = (productId: string, price: number, currency?: string): Product => ({
  id: productId,
  name: "Item",
  description: "",
  price,
  currency,
  images: [],
});

type CartResponse = {
  id?: string;
  items?: Array<{
    productId: string;
    quantity: number;
    price?: string;
    currency?: string;
  }>;
  subtotal?: string | number;
  discountTotal?: string | number;
  shippingEstimate?: string | number;
  total?: string | number;
  currency?: string;
};

const toCart = (payload: CartResponse | unknown): Cart => {
  const data = (payload as CartResponse) ?? {};
  const items =
    data?.items?.map((item) => {
      const unitPrice = parseFloat(item.price ?? "0");
      const quantity = item.quantity ?? 0;
      return {
        id: item.productId,
        product: toProduct(item.productId, unitPrice, item.currency),
        quantity,
        unitPrice,
        subtotal: unitPrice * quantity,
      };
    }) ?? [];
  const subtotal = items.reduce((sum: number, i) => sum + i.subtotal, 0);
  return {
    id: data?.id ?? "local",
    items,
    subtotal,
    discountTotal: parseFloat((data?.discountTotal ?? "0").toString()),
    shippingEstimate: parseFloat((data?.shippingEstimate ?? "0").toString()),
    total: parseFloat((data?.total ?? subtotal).toString()),
    currency: data?.currency,
  };
};

export const cartApi = {
  get: async () => toCart(await apiRequest("/api/carts") as CartResponse),
  addItem: (payload: AddToCartRequest) =>
    apiRequest("/api/carts/items", {
      method: "POST",
      body: {
        productId: payload.productId,
        quantity: payload.quantity,
        price: payload.price?.toString(),
        currency: payload.currency,
        cartId: payload.cartId,
      },
    }).then((resp) => toCart(resp as CartResponse)),
  updateItem: (payload: UpdateCartItemRequest) =>
    apiRequest(`/api/carts/items/${payload.itemId}`, {
      method: "PATCH",
      body: { quantity: payload.quantity },
    }).then((resp) => toCart(resp as CartResponse)),
  removeItem: (itemId: string) =>
    apiRequest(`/api/carts/items/${itemId}`, { method: "DELETE" }).then((resp) => toCart(resp as CartResponse)),
  merge: (items: AddToCartRequest[]) =>
    apiRequest("/api/carts/merge", {
      method: "POST",
      body: {
        items: items.map((item) => ({
          productId: item.productId,
          quantity: item.quantity,
          price: item.price?.toString(),
          currency: item.currency,
          cartId: item.cartId,
        })),
      },
    }).then((resp) => toCart(resp as CartResponse)),
  clear: () => apiRequest("/api/carts/clear", { method: "POST" }).then((resp) => toCart(resp as CartResponse)),
};
