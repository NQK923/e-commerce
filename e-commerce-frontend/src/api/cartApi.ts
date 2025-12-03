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

const toCart = (payload: any): Cart => {
  const items =
    payload?.items?.map((item: any) => {
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
  const subtotal = items.reduce((sum: number, i: any) => sum + i.subtotal, 0);
  return {
    id: payload?.id ?? "local",
    items,
    subtotal,
    discountTotal: parseFloat(payload?.discountTotal ?? "0"),
    shippingEstimate: parseFloat(payload?.shippingEstimate ?? "0"),
    total: parseFloat(payload?.total ?? subtotal),
    currency: payload?.currency,
  };
};

export const cartApi = {
  get: async () => toCart(await apiRequest("/api/carts")),
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
    }).then(toCart),
  updateItem: (payload: UpdateCartItemRequest) =>
    apiRequest(`/api/carts/items/${payload.itemId}`, {
      method: "PATCH",
      body: { quantity: payload.quantity },
    }).then(toCart),
  removeItem: (itemId: string) =>
    apiRequest(`/api/carts/items/${itemId}`, { method: "DELETE" }).then(toCart),
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
    }).then(toCart),
  clear: () => apiRequest("/api/carts/clear", { method: "POST" }).then(toCart),
};
