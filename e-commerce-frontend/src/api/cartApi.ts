import { apiRequest } from "../lib/api-client";
import { AddToCartRequest, Cart, UpdateCartItemRequest } from "../types/cart";
import { Product } from "../types/product";
import { productApi } from "./productApi";

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
      const unitPrice = Number.parseFloat(item.price ?? "0");
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
    discountTotal: Number.parseFloat((data?.discountTotal ?? "0").toString()),
    shippingEstimate: Number.parseFloat((data?.shippingEstimate ?? "0").toString()),
    total: Number.parseFloat((data?.total ?? subtotal).toString()),
    currency: data?.currency,
  };
};

const enrichCartProducts = async (cart: Cart): Promise<Cart> => {
  if (!cart.items.length) return cart;

  const enrichedItems = await Promise.all(
    cart.items.map(async (item) => {
      try {
        const detail = await productApi.detail(item.product.id);
        const price = detail.price ?? item.unitPrice ?? 0;
        return {
          ...item,
          unitPrice: price,
          subtotal: price * item.quantity,
          product: {
            ...detail,
            price,
            currency: detail.currency ?? item.product.currency ?? cart.currency,
            images: detail.images ?? [],
          },
        };
      } catch {
        return item;
      }
    }),
  );

  const subtotal = enrichedItems.reduce((sum, i) => sum + i.subtotal, 0);
  const currency = enrichedItems[0]?.product.currency ?? cart.currency;

  return {
    ...cart,
    items: enrichedItems,
    subtotal,
    total: cart.total ?? subtotal,
    currency,
  };
};

export const cartApi = {
  get: async () => enrichCartProducts(toCart(await apiRequest("/api/carts") as CartResponse)),
  addItem: async (payload: AddToCartRequest) =>
    enrichCartProducts(
      toCart(
        await apiRequest("/api/carts/items", {
          method: "POST",
          body: {
            productId: payload.productId,
            quantity: payload.quantity,
            price: payload.price?.toString(),
            currency: payload.currency,
            cartId: payload.cartId,
          },
        }) as CartResponse,
      ),
    ),
  updateItem: async (payload: UpdateCartItemRequest) =>
    enrichCartProducts(
      toCart(
        await apiRequest(`/api/carts/items/${payload.itemId}`, {
          method: "PATCH",
          body: { quantity: payload.quantity },
        }) as CartResponse,
      ),
    ),
  removeItem: async (itemId: string) =>
    enrichCartProducts(toCart(await apiRequest(`/api/carts/items/${itemId}`, { method: "DELETE" }) as CartResponse)),
  merge: async (items: AddToCartRequest[]) =>
    enrichCartProducts(
      toCart(
        await apiRequest("/api/carts/merge", {
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
        }) as CartResponse,
      ),
    ),
  clear: async () => enrichCartProducts(toCart(await apiRequest("/api/carts/clear", { method: "POST" }) as CartResponse)),
};
