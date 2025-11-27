import { apiRequest } from "../lib/api-client";
import { AddToCartRequest, Cart, UpdateCartItemRequest } from "../types/cart";

export const cartApi = {
  get: () => apiRequest<Cart>("/api/cart"),
  addItem: (payload: AddToCartRequest) =>
    apiRequest<Cart>("/api/cart/items", { method: "POST", body: payload }),
  updateItem: (payload: UpdateCartItemRequest) =>
    apiRequest<Cart>(`/api/cart/items/${payload.itemId}`, {
      method: "PATCH",
      body: { quantity: payload.quantity },
    }),
  removeItem: (itemId: string) =>
    apiRequest<Cart>(`/api/cart/items/${itemId}`, { method: "DELETE" }),
  merge: (items: AddToCartRequest[]) =>
    apiRequest<Cart>("/api/cart/merge", { method: "POST", body: { items } }),
  clear: () => apiRequest<Cart>("/api/cart/clear", { method: "POST" }),
};
