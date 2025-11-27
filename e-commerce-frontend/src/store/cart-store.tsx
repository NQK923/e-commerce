'use client';

import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { cartApi } from "../api/cartApi";
import { useAuth } from "./auth-store";
import { AddToCartRequest, Cart, CartItem } from "../types/cart";
import { Product } from "../types/product";

type CartContextValue = {
  cart: Cart | null;
  loading: boolean;
  addItem: (product: Product, quantity?: number) => Promise<void>;
  updateQuantity: (itemId: string, quantity: number) => Promise<void>;
  removeItem: (itemId: string) => Promise<void>;
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
    const parsed = JSON.parse(saved) as Cart;
    return parsed;
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

export const CartProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user, initializing } = useAuth();
  const [cart, setCart] = useState<Cart | null>(null);
  const [loading, setLoading] = useState<boolean>(false);

  const useLocalCart = !user;

  const setAndPersistLocal = useCallback((next: Cart | null) => {
    setCart(next);
    persistLocalCart(next);
  }, []);

  const refreshCart = useCallback(async () => {
    if (!user) {
      const stored = loadLocalCart();
      setCart(stored);
      return;
    }
    setLoading(true);
    try {
      const serverCart = await cartApi.get();
      setCart(serverCart);
    } finally {
      setLoading(false);
    }
  }, [user]);

  const addItem = useCallback(
    async (product: Product, quantity = 1) => {
      if (useLocalCart) {
        const existingItems = loadLocalCart()?.items ?? [];
        const found = existingItems.find((item) => item.product.id === product.id);
        const updatedItems = found
          ? existingItems.map((item) =>
              item.product.id === product.id
                ? { ...item, quantity: item.quantity + quantity, subtotal: (item.quantity + quantity) * item.unitPrice }
                : item,
            )
          : [
              ...existingItems,
              {
                id: product.id,
                product,
                quantity,
                unitPrice: product.price,
                subtotal: product.price * quantity,
              },
            ];
        const nextCart = calculateTotals(updatedItems);
        setAndPersistLocal(nextCart);
        return;
      }

      setLoading(true);
      try {
        const serverCart = await cartApi.addItem({ productId: product.id, quantity });
        setCart(serverCart);
      } finally {
        setLoading(false);
      }
    },
    [setAndPersistLocal, useLocalCart],
  );

  const updateQuantity = useCallback(
    async (itemId: string, quantity: number) => {
      if (useLocalCart) {
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
        return;
      }

      setLoading(true);
      try {
        const serverCart = await cartApi.updateItem({ itemId, quantity });
        setCart(serverCart);
      } finally {
        setLoading(false);
      }
    },
    [setAndPersistLocal, useLocalCart],
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
        const serverCart = await cartApi.removeItem(itemId);
        setCart(serverCart);
      } finally {
        setLoading(false);
      }
    },
    [setAndPersistLocal, useLocalCart],
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
    // Merge local cart to server on login.
    const mergeAndLoad = async () => {
      if (!user) return;
      const local = loadLocalCart();
      if (local?.items?.length) {
        const items: AddToCartRequest[] = local.items.map((item) => ({
          productId: item.product.id,
          quantity: item.quantity,
        }));
        try {
          await cartApi.merge(items);
        } finally {
          persistLocalCart(null);
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
      refreshCart,
      clearCart,
    }),
    [addItem, cart, clearCart, loading, refreshCart, removeItem, updateQuantity],
  );

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
};

export const useCart = () => {
  const ctx = useContext(CartContext);
  if (!ctx) throw new Error("useCart must be used within CartProvider");
  return ctx;
};
