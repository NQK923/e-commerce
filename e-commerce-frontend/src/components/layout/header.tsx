'use client';

import Link from "next/link";
import { usePathname } from "next/navigation";
import React from "react";
import { ShoppingBag, User, Search, Menu, LogOut } from "lucide-react";
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
    <header className="sticky top-0 z-40 border-b border-zinc-200 bg-white/80 backdrop-blur-md">
      <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-4">
        {/* Left: Logo & Desktop Nav */}
        <div className="flex items-center gap-8">
          <Link href="/" className="flex items-center gap-2 text-2xl font-bold tracking-tighter text-black">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-black text-white">
              <ShoppingBag size={18} />
            </div>
            Store
          </Link>

          <nav className="hidden items-center gap-6 text-sm font-medium text-zinc-500 md:flex">
            {navItems
              .filter((item) => (item.requiresAuth ? Boolean(user) : true))
              .map((item) => (
                <Link
                  key={item.href}
                  href={item.href}
                  className={cx(
                    "transition-colors hover:text-black",
                    pathname === item.href ? "text-black font-semibold" : "text-zinc-500",
                  )}
                >
                  {item.label}
                </Link>
              ))}
          </nav>
        </div>

        {/* Right: Actions */}
        <div className="flex items-center gap-4">
          {/* Search - Hidden on mobile for now or icon only */}
          <button className="hidden p-2 text-zinc-500 transition-colors hover:bg-zinc-100 hover:text-black sm:block sm:rounded-full">
            <Search size={20} />
          </button>

          {/* Cart */}
          <Link href="/cart" className="group relative p-2 text-zinc-800 hover:text-black">
            <ShoppingBag size={22} />
            {itemCount > 0 && (
              <span className="absolute -right-1 -top-1 flex h-5 w-5 items-center justify-center rounded-full bg-black text-[10px] font-bold text-white ring-2 ring-white transition-transform group-hover:scale-110">
                {itemCount}
              </span>
            )}
          </Link>

          {/* Auth */}
          <div className="flex items-center gap-2 border-l border-zinc-200 pl-4">
            {user ? (
              <div className="flex items-center gap-3">
                <Link href="/profile" className="hidden flex items-center gap-2 text-sm font-medium text-zinc-700 hover:text-black sm:flex">
                  <User size={18} />
                  {user.displayName || "Account"}
                </Link>
                <Button 
                  variant="ghost" 
                  size="icon" 
                  onClick={() => logout()} 
                  title="Logout"
                  className="h-9 w-9 text-zinc-500 hover:text-red-600"
                >
                  <LogOut size={18} />
                </Button>
              </div>
            ) : (
              <div className="flex items-center gap-2">
                <Link href="/login">
                  <Button size="sm" variant="ghost" className="font-semibold">
                    Log in
                  </Button>
                </Link>
                <Link href="/register">
                  <Button size="sm" className="rounded-full px-5">
                    Sign up
                  </Button>
                </Link>
              </div>
            )}
          </div>
          
          {/* Mobile Menu Toggle */}
          <button className="block md:hidden p-2 text-zinc-800">
            <Menu size={24} />
          </button>
        </div>
      </div>
    </header>
  );
};
