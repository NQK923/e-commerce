'use client';

import React from "react";
import { AuthProvider } from "../store/auth-store";
import { CartProvider } from "../store/cart-store";
import { ToastProvider } from "../components/ui/toast-provider";
import { LanguageProvider } from "./language-provider";

type AppProvidersProps = {
  children: React.ReactNode;
  initialLanguage?: "en" | "vi";
};

export const AppProviders: React.FC<AppProvidersProps> = ({ children, initialLanguage }) => {
  return (
    <LanguageProvider initialLanguage={initialLanguage}>
      <ToastProvider>
        <AuthProvider>
          <CartProvider>{children}</CartProvider>
        </AuthProvider>
      </ToastProvider>
    </LanguageProvider>
  );
};
