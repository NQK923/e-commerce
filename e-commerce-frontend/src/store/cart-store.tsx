'use client';

import React, {createContext, useCallback, useContext, useEffect, useMemo, useState} from "react";
import {cartApi} from "../api/cartApi";
import {useAuth} from "./auth-store";
import {AddToCartRequest, Cart, CartItem} from "../types/cart";
import {Product} from "../types/product";
import {useToast} from "../components/ui/toast-provider";

type CartContextValue = {
  cart: Cart | null;
  loading: boolean;
  addItem: (product: Product, quantity?: number, variantSku?: string) => Promise<void>;
  updateQuantity: (itemId: string, quantity: number) => Promise<void>;
  removeItem: (itemId: string) => Promise<void>;
  changeVariant: (itemId: string, newVariantSku: string) => Promise<void>;
  refreshCart: () => Promise<void>;
  clearCart: () => Promise<void>;
};

const CartContext = createContext<CartContextValue | undefined>(undefined);

const LOCAL_CART_KEY = "ecommerce_cart";

const calculateTotals = (items: CartItem[]): Cart => {
  const subtotal = items.reduce((sum, item) => sum + item.unitPrice * item.quantity, 0);
  return {
    id: "local",
    items,
    subtotal,
    discountTotal: 0,
    shippingEstimate: 0,
    total: subtotal,
    currency: items[0]?.product.currency ?? "USD",
  };
};

const loadLocalCart = (): Cart | null => {
  if (typeof window === "undefined") return null;
  const saved = localStorage.getItem(LOCAL_CART_KEY);
  if (!saved) return null;
  try {
      return JSON.parse(saved) as Cart;
  } catch {
    return null;
  }
};

const persistLocalCart = (cart: Cart | null) => {
  if (typeof window === "undefined") return;
  if (cart) {
    localStorage.setItem(LOCAL_CART_KEY, JSON.stringify(cart));
  } else {
    localStorage.removeItem(LOCAL_CART_KEY);
  }
};

const parseItemId = (itemId: string) => {
    if (!itemId) return { productId: "", variantSku: undefined };
    const parts = itemId.split(":");
    if (parts.length > 1) {
        return { productId: parts[0], variantSku: parts[1] };
    }
    return { productId: itemId, variantSku: undefined };
};

const resolveVariantInfo = (product: Product, variantSku?: string) => {
  const variant = variantSku ? product.variants?.find((v) => v.sku === variantSku) : undefined;
  const price = variant?.price ?? product.price ?? 0;
  const stock = variant?.quantity ?? product.stock;
  return { price, stock };
};

export const CartProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user, initializing } = useAuth();
  const { addToast } = useToast();
  const [cart, setCart] = useState<Cart | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [serverCartUnavailable, setServerCartUnavailable] = useState<boolean>(false);
  const [serverCartId, setServerCartId] = useState<string | null>(null);

  const useLocalCart = !user || serverCartUnavailable;

  const setAndPersistLocal = useCallback((next: Cart | null) => {
    setCart(next);
    persistLocalCart(next);
  }, []);

  const addItemToLocal = useCallback(
    (product: Product, quantity = 1, variantSku?: string) => {
      const { price: unitPrice, stock } = resolveVariantInfo(product, variantSku);
      const available = stock ?? Number.POSITIVE_INFINITY;
      if (available <= 0) {
        addToast("Product is out of stock", "error");
        return;
      }
      const existingItems = loadLocalCart()?.items ?? [];
      const found = existingItems.find((item) => item.product.id === product.id && item.variantSku === variantSku);
      const nextQuantity = Math.min(
        available,
        found ? found.quantity + quantity : quantity,
      );
      if (nextQuantity <= 0) {
        addToast("Quantity is not valid", "error");
        return;
      }
      
      const itemId = variantSku ? `${product.id}:${variantSku}` : product.id;

      const updatedItems = found
        ? existingItems.map((item) =>
            (item.product.id === product.id && item.variantSku === variantSku)
              ? { 
                  ...item, 
                  quantity: nextQuantity, 
                  unitPrice, 
                  subtotal: nextQuantity * unitPrice,
                  product: { ...item.product, price: unitPrice },
                }
              : item,
          )
        : [
            ...existingItems,
            {
              id: itemId,
              product: { ...product, price: unitPrice },
              variantSku,
              quantity: nextQuantity,
              unitPrice,
              subtotal: unitPrice * nextQuantity,
            },
          ];
      const nextCart = calculateTotals(updatedItems);
      setAndPersistLocal(nextCart);
    },
    [addToast, setAndPersistLocal],
  );

  const refreshCart = useCallback(async () => {
    if (!user) {
      const stored = loadLocalCart();
      setCart(stored);
      return;
    }
    if (serverCartUnavailable) {
      const stored = loadLocalCart();
      setCart(stored);
      return;
    }
    setLoading(true);
    try {
      const serverCart = await cartApi.get();
      setCart(serverCart);
      setServerCartId(serverCart?.id ?? null);
    } catch (error) {
      console.error("Failed to refresh server cart, falling back to local cart", error);
      setServerCartUnavailable(true);
      setServerCartId(null);
      const stored = loadLocalCart();
      setCart(stored);
    } finally {
      setLoading(false);
    }
  }, [serverCartUnavailable, user]);

  const addItem = useCallback(
    async (product: Product, quantity = 1, variantSku?: string) => {
      if (useLocalCart) {
        addItemToLocal(product, quantity, variantSku);
        return;
      }

      setLoading(true);
      try {
        const { price: unitPrice, stock } = resolveVariantInfo(product, variantSku);
        const available = stock ?? Number.POSITIVE_INFINITY;
        if (available <= 0) {
          addToast("Product is out of stock", "error");
          return;
        }
        const desired = Math.min(quantity, available);
        const serverCart = await cartApi.addItem({
          productId: product.id,
          variantSku,
          quantity: desired,
          price: unitPrice,
          currency: product.currency ?? "USD",
          cartId: serverCartId ?? undefined,
        });
        setCart(serverCart);
        setServerCartId(serverCart?.id ?? null);
      } catch (error) {
        console.error("Failed to add item to server cart, using local cart instead", error);
        setServerCartUnavailable(true);
        setServerCartId(null);
        addItemToLocal(product, quantity, variantSku);
      } finally {
        setLoading(false);
      }
    },
    [addItemToLocal, addToast, serverCartId, useLocalCart],
  );

  const updateQuantity = useCallback(
    async (itemId: string, quantity: number) => {
      if (useLocalCart) {
        const existingItems = loadLocalCart()?.items ?? [];
        const updatedItems = existingItems
          .map((item) => {
            if (item.id !== itemId) return item;
            const max = item.product.stock ?? quantity;
            const clamped = Math.min(Math.max(quantity, 0), max);
            return { ...item, quantity: clamped, subtotal: item.unitPrice * clamped };
          })
          .filter((item) => item.quantity > 0);
        const nextCart = updatedItems.length ? calculateTotals(updatedItems) : null;
        setAndPersistLocal(nextCart);
        return;
      }

      setLoading(true);
      try {
        const currentItem = cart?.items.find((i) => i.id === itemId);
        const { price: unitPrice, stock: variantStock } = currentItem
          ? resolveVariantInfo(currentItem.product, variantSku ?? currentItem.variantSku)
          : { price: 0, stock: undefined };
        const max = variantStock ?? currentItem?.product.stock ?? quantity;
        const clamped = Math.min(Math.max(quantity, 0), max);
        const { productId, variantSku } = parseItemId(itemId);
        
        const serverCart = await cartApi.updateItem({ 
            itemId: productId, 
            variantSku,
            quantity: clamped,
            price: unitPrice,
            currency: currentItem?.product.currency ?? cart?.currency ?? "USD",
        });
        setCart(serverCart);
        setServerCartId(serverCart?.id ?? null);
      } catch (error) {
        console.error("Failed to update server cart, using local cart instead", error);
        setServerCartUnavailable(true);
        setServerCartId(null);
        const existingItems = loadLocalCart()?.items ?? [];
        const updatedItems = existingItems
          .map((item) =>
            item.id === itemId
              ? { ...item, quantity, subtotal: item.unitPrice * quantity }
              : item,
          )
          .filter((item) => item.quantity > 0);
        const nextCart = updatedItems.length ? calculateTotals(updatedItems) : null;
        setAndPersistLocal(nextCart);
      } finally {
        setLoading(false);
      }
    },
    [cart, setAndPersistLocal, useLocalCart],
  );

  const removeItem = useCallback(
    async (itemId: string) => {
      if (useLocalCart) {
        const existingItems = loadLocalCart()?.items ?? [];
        const updatedItems = existingItems.filter((item) => item.id !== itemId);
        const nextCart = updatedItems.length ? calculateTotals(updatedItems) : null;
        setAndPersistLocal(nextCart);
        return;
      }

      setLoading(true);
      try {
        const { productId, variantSku } = parseItemId(itemId);
        const serverCart = await cartApi.removeItem(productId, variantSku);
        setCart(serverCart);
        setServerCartId(serverCart?.id ?? null);
      } catch (error) {
        console.error("Failed to remove item from server cart, using local cart instead", error);
        setServerCartUnavailable(true);
        setServerCartId(null);
        const existingItems = loadLocalCart()?.items ?? [];
        const updatedItems = existingItems.filter((item) => item.id !== itemId);
        const nextCart = updatedItems.length ? calculateTotals(updatedItems) : null;
        setAndPersistLocal(nextCart);
      } finally {
        setLoading(false);
      }
    },
    [setAndPersistLocal, useLocalCart],
  );

  const changeVariant = useCallback(
    async (itemId: string, newVariantSku: string) => {
      const item = cart?.items.find((i) => i.id === itemId);
      if (!item) return;

      // If sku is same, do nothing
      if (item.variantSku === newVariantSku) return;

      const { price: unitPrice, stock } = resolveVariantInfo(item.product, newVariantSku);
      const targetId = newVariantSku ? `${item.product.id}:${newVariantSku}` : item.product.id;

      if (useLocalCart) {
        const existingItems = loadLocalCart()?.items ?? [];
        const filtered = existingItems.filter((i) => i.id !== itemId);
        const target = filtered.find((i) => i.id === targetId);
        const available = stock ?? Number.POSITIVE_INFINITY;
        const desiredQty = Math.min(item.quantity + (target?.quantity ?? 0), available);
        const updated = target
          ? filtered.map((i) =>
              i.id === targetId
                ? {
                    ...i,
                    quantity: desiredQty,
                    variantSku: newVariantSku,
                    unitPrice,
                    subtotal: unitPrice * desiredQty,
                    product: { ...i.product, price: unitPrice },
                  }
                : i,
            )
          : [
              ...filtered,
              {
                ...item,
                id: targetId,
                variantSku: newVariantSku,
                quantity: desiredQty,
                unitPrice,
                subtotal: unitPrice * desiredQty,
                product: { ...item.product, price: unitPrice },
              },
            ];
        const nextCart = calculateTotals(updated);
        setAndPersistLocal(nextCart);
        return;
      }

      setLoading(true);
      try {
        const { productId, variantSku: oldVariantSku } = parseItemId(itemId);
        // remove old variant then add new to avoid duplicates
        await cartApi.removeItem(productId, oldVariantSku);
        const serverCart = await cartApi.addItem({
          productId,
          variantSku: newVariantSku,
          quantity: item.quantity,
          price: unitPrice,
          currency: item.product.currency ?? cart?.currency ?? "USD",
          cartId: serverCartId ?? undefined,
        });
        setCart(serverCart);
        setServerCartId(serverCart?.id ?? null);
        addToast("Variant updated", "success");
      } catch (error) {
        console.error("Failed to change variant", error);
        addToast("Failed to update variant", "error");
        setServerCartUnavailable(true);
      } finally {
        setLoading(false);
      }
    },
    [addToast, cart, serverCartId, setAndPersistLocal, useLocalCart],
  );

  const clearCart = useCallback(async () => {
    if (useLocalCart) {
      setAndPersistLocal(null);
      return;
    }
    setLoading(true);
    try {
      const serverCart = await cartApi.clear().catch(() => null);
      setCart(serverCart);
      setServerCartId(serverCart?.id ?? null);
    } catch (error) {
      console.error("Failed to clear server cart, using local cart instead", error);
      setServerCartUnavailable(true);
      setServerCartId(null);
      setAndPersistLocal(null);
    } finally {
      setLoading(false);
    }
  }, [setAndPersistLocal, useLocalCart]);

  useEffect(() => {
    // Load initial cart when auth state resolves.
    if (initializing) return;
    void refreshCart();
  }, [initializing, refreshCart]);

  useEffect(() => {
    // Reset server cart state when auth changes.
    setServerCartUnavailable(false);
    setServerCartId(null);
  }, [user]);

  useEffect(() => {
    // Merge local cart to server on login.
    const mergeAndLoad = async () => {
      if (!user) return;
      const local = loadLocalCart();
      if (local?.items?.length) {
        const items: AddToCartRequest[] = local.items.map((item) => ({
          productId: item.product.id,
          variantSku: item.variantSku,
          quantity: item.quantity,
          price: item.unitPrice,
          currency: item.product.currency ?? "USD",
        }));
        let merged = false;
        try {
          await cartApi.merge(items);
          merged = true;
        } catch (error) {
          console.error("Failed to merge local cart to server, keeping local cart", error);
          setServerCartUnavailable(true);
        } finally {
          if (merged) {
            persistLocalCart(null);
          }
        }
      }
      await refreshCart();
    };
    void mergeAndLoad();
  }, [refreshCart, user]);

  const value = useMemo(
    () => ({
      cart,
      loading,
      addItem,
      updateQuantity,
      removeItem,
      changeVariant,
      refreshCart,
      clearCart,
    }),
    [addItem, cart, clearCart, loading, refreshCart, removeItem, updateQuantity, changeVariant],
  );

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
};

export const useCart = () => {
  const ctx = useContext(CartContext);
  if (!ctx) throw new Error("useCart must be used within CartProvider");
  return ctx;
};
