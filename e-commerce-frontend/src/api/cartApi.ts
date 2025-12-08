import { apiRequest } from "../lib/api-client";
import { AddToCartRequest, Cart, CartItem, UpdateCartItemRequest } from "../types/cart";
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

const parseNumber = (value: string | number | undefined | null, fallback = 0) => {
  const num = Number.parseFloat((value ?? "").toString());
  return Number.isFinite(num) ? num : fallback;
};

const clampQuantity = (quantity: number, stock?: number) => {
  if (stock === undefined || stock === null) return Math.max(quantity, 0);
  return Math.max(0, Math.min(quantity, stock));
};

const toCart = (payload: CartResponse | unknown): Cart => {
  const data = (payload as CartResponse) ?? {};
  const items =
    data?.items?.map((item) => {
      const unitPrice = parseNumber(item.price, 0);
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
  const discount = parseNumber(data?.discountTotal, 0);
  const shipping = parseNumber(data?.shippingEstimate, 0);
  const currency = data?.currency ?? items[0]?.product.currency ?? "USD";
  const totalFromServer = parseNumber(data?.total, subtotal + shipping - discount);

  return {
    id: data?.id ?? "local",
    items,
    subtotal,
    discountTotal: discount,
    shippingEstimate: shipping,
    total: totalFromServer || subtotal + shipping - discount,
    currency,
  };
};

const enrichCartProducts = async (cart: Cart): Promise<Cart> => {
  if (!cart.items.length) return { ...cart, subtotal: 0, total: 0 };

  const enrichedItems = await Promise.all(
    cart.items.map(async (item) => {
      try {
        const detail = await productApi.detail(item.product.id);
        const price = Number.isFinite(detail.price) ? detail.price : item.unitPrice ?? 0;
        const stock = detail.stock ?? item.product.stock;
        const quantity = clampQuantity(item.quantity, stock);
        if (quantity <= 0) return null;
        return {
          ...item,
          quantity,
          unitPrice: price,
          subtotal: price * quantity,
          product: {
            ...detail,
            price,
            currency: detail.currency ?? item.product.currency ?? cart.currency ?? "USD",
            images: detail.images ?? [],
            stock,
          },
        };
      } catch {
        const quantity = clampQuantity(item.quantity, item.product.stock);
        if (quantity <= 0) return null;
        const unitPrice = Number.isFinite(item.unitPrice) ? item.unitPrice : 0;
        return { ...item, quantity, subtotal: unitPrice * quantity };
      }
    }),
  );

  const filtered = enrichedItems.filter((i): i is CartItem => Boolean(i));
  const subtotal = filtered.reduce((sum, i) => sum + i.subtotal, 0);
  const currency = filtered[0]?.product.currency ?? cart.currency ?? "USD";
  const discount = cart.discountTotal ?? 0;
  const shipping = cart.shippingEstimate ?? 0;
  const totalFromServer = cart.total ?? 0;
  const recalculatedTotal = totalFromServer > 0 ? totalFromServer : subtotal + shipping - discount;

  return {
    ...cart,
    items: filtered,
    subtotal,
    total: recalculatedTotal,
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
