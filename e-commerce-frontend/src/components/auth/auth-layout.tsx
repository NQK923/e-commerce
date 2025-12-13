import React from "react";
import Link from "next/link";
import { ShoppingBag } from "lucide-react";

type AuthLayoutProps = {
  children: React.ReactNode;
  title: string;
  subtitle: string;
};

export const AuthLayout: React.FC<AuthLayoutProps> = ({ children, title, subtitle }) => {
  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-emerald-50 via-white to-teal-50 px-4 py-12 sm:px-6 lg:px-8">
      <div className="w-full max-w-md space-y-8">
        {/* Header */}
        <div className="flex flex-col items-center text-center">
          <Link href="/" className="group mb-6 flex items-center justify-center gap-2">
            <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-emerald-600 text-white shadow-lg shadow-emerald-600/20 transition-transform group-hover:scale-110 group-hover:rotate-3">
              <ShoppingBag size={24} />
            </div>
            <span className="text-2xl font-bold tracking-tight text-zinc-900">ShopeeClone</span>
          </Link>
          <h2 className="text-3xl font-extrabold tracking-tight text-zinc-900">{title}</h2>
          <p className="mt-2 text-sm text-zinc-600">{subtitle}</p>
        </div>

        {/* Card */}
        <div className="rounded-3xl bg-white px-8 py-10 shadow-2xl shadow-zinc-200/50 sm:px-10 border border-zinc-100">
          {children}
        </div>
        
        {/* Footer Links (Optional, maybe specific to page but good to have a slot or just let children handle it) */}
      </div>
    </div>
  );
};
