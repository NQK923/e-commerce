'use client';

import Link from "next/link";
import { usePathname } from "next/navigation";
import React from "react";
import { useAuth } from "../../store/auth-store";
import { useCart } from "../../store/cart-store";
import { cx } from "../../utils/cx";
import { Button } from "../ui/button";

const navItems = [
  { href: "/", label: "Home" },
  { href: "/products", label: "Products" },
  { href: "/orders", label: "Orders", requiresAuth: true },
];

export const Header: React.FC = () => {
  const pathname = usePathname();
  const { user, logout } = useAuth();
  const { cart } = useCart();
  const itemCount = cart?.items.reduce((sum, item) => sum + item.quantity, 0) ?? 0;

  return (
    <header className="sticky top-0 z-40 border-b border-zinc-200 bg-white/70 backdrop-blur">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3">
        <Link href="/" className="flex items-center gap-2 text-xl font-bold tracking-tight text-black">
          eCommerce
        </Link>
        <nav className="flex items-center gap-6 text-sm font-medium text-zinc-700">
          {navItems
            .filter((item) => (item.requiresAuth ? Boolean(user) : true))
            .map((item) => (
              <Link
                key={item.href}
                href={item.href}
                className={cx(
                  "hover:text-black",
                  pathname === item.href ? "text-black" : "text-zinc-600",
                )}
              >
                {item.label}
              </Link>
            ))}
          <Link href="/cart" className="relative">
            <span className={cx("hover:text-black", pathname === "/cart" ? "text-black" : "text-zinc-600")}>
              Cart
            </span>
            {itemCount > 0 && (
              <span className="absolute -right-3 -top-2 rounded-full bg-black px-1.5 py-0.5 text-[10px] font-semibold text-white">
                {itemCount}
              </span>
            )}
          </Link>
        </nav>
        <div className="flex items-center gap-3">
          {user ? (
            <>
              <Link href="/profile" className="text-sm font-semibold text-black hover:underline">
                {user.displayName || user.email}
              </Link>
              <Button variant="secondary" size="sm" onClick={() => logout()}>
                Logout
              </Button>
            </>
          ) : (
            <>
              <Link href="/login">
                <Button size="sm" variant="secondary">
                  Login
                </Button>
              </Link>
              <Link href="/register">
                <Button size="sm">Sign up</Button>
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
};
