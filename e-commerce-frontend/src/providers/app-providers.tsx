'use client';

import React from "react";
import { AuthProvider } from "../store/auth-store";
import { CartProvider } from "../store/cart-store";
import { ToastProvider } from "../components/ui/toast-provider";

export const AppProviders: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <ToastProvider>
    <AuthProvider>
      <CartProvider>{children}</CartProvider>
    </AuthProvider>
  </ToastProvider>
);
