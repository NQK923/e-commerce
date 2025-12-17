'use client';

import Link from "next/link";
import { usePathname } from "next/navigation";
import React from "react";
import { ShoppingBag, User, Search, Menu, LogOut, Store, Shield } from "lucide-react";
import { useAuth } from "../../store/auth-store";
import { useCart } from "../../store/cart-store";
import { cx } from "../../utils/cx";
import { Button } from "../ui/button";
import { useTranslation } from "../../providers/language-provider";
import { NotificationBell } from "../notifications/notification-bell";

export const Header: React.FC = () => {
  const pathname = usePathname();
  const { user, logout } = useAuth();
  const { cart } = useCart();
  const { t } = useTranslation();
  const [mounted, setMounted] = React.useState(false);

  React.useEffect(() => {
    setMounted(true);
  }, []);

  const itemCount = cart?.items.reduce((sum, item) => sum + item.quantity, 0) ?? 0;

  const isAdmin = mounted && user?.roles?.includes("ADMIN");

  const navItems = [
    { href: "/", label: t.nav.home },
    { href: "/products", label: t.nav.products },
    { href: "/orders", label: t.nav.orders, requiresAuth: true },
    ...(isAdmin ? [{ href: "/admin", label: "Admin" }] : []),
  ];

  if (pathname?.startsWith('/admin')) {
    return null;
  }

  return (
    <header className="sticky top-0 z-40 border-b border-zinc-200 bg-white/80 backdrop-blur-md">
      <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-4">
        {/* Left: Logo & Desktop Nav */}
        <div className="flex items-center gap-8">
          <Link href="/" className="flex items-center gap-2 text-2xl font-bold tracking-tighter text-emerald-800">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-emerald-600 text-white shadow-sm">
              <ShoppingBag size={18} />
            </div>
            EcomX
          </Link>

          <nav className="hidden items-center gap-6 text-sm font-medium text-zinc-500 md:flex">
            {/* Seller Channel Link */}
            <Link 
              href="/seller/dashboard" 
              className="flex items-center gap-1 font-semibold text-zinc-600 transition-colors hover:text-emerald-600"
            >
              <Store size={16} />
              <span suppressHydrationWarning>{t.nav.sellerChannel}</span>
            </Link>

            {isAdmin && (
              <Link 
                href="/admin" 
                className="flex items-center gap-1 font-semibold text-zinc-600 transition-colors hover:text-emerald-600"
              >
                <Shield size={16} />
                Admin
              </Link>
            )}
            
            <div className="h-4 w-px bg-zinc-200 mx-1" />

            {navItems
              .filter((item) => (item.requiresAuth ? (mounted && Boolean(user)) : true))
              .map((item) => (
                <Link
                  key={item.href}
                  href={item.href}
                  className={cx(
                    "transition-colors hover:text-emerald-600",
                    pathname === item.href ? "text-emerald-700 font-bold" : "text-zinc-600",
                  )}
                >
                  <span suppressHydrationWarning>{item.label}</span>
                </Link>
              ))}
          </nav>
        </div>

        {/* Right: Actions */}
        <div className="flex items-center gap-4">
          {/* Search - Hidden on mobile for now or icon only */}
          <button className="hidden p-2 text-zinc-500 transition-colors hover:bg-emerald-50 hover:text-emerald-600 sm:block sm:rounded-full">
            <Search size={20} />
          </button>

          <NotificationBell />

          {/* Cart */}
          <Link href="/cart" className="group relative p-2 text-zinc-600 hover:text-emerald-600 transition-colors">
            <ShoppingBag size={22} />
            {mounted && itemCount > 0 && (
              <span className="absolute -right-1 -top-1 flex h-5 w-5 items-center justify-center rounded-full bg-emerald-600 text-[10px] font-bold text-white ring-2 ring-white transition-transform group-hover:scale-110 shadow-sm">
                {itemCount}
              </span>
            )}
          </Link>

          {/* Auth */}
          <div className="flex items-center gap-2 border-l border-zinc-200 pl-4">
            {mounted && user ? (
              <div className="flex items-center gap-3">
                <Link href="/profile" className="hidden flex items-center gap-2 text-sm font-medium text-zinc-600 hover:text-emerald-600 sm:flex">
                  <User size={18} />
                  <span suppressHydrationWarning>{user.displayName || t.nav.account}</span>
                </Link>
                <Button 
                  variant="ghost" 
                  size="icon" 
                  onClick={() => logout()} 
                  title={t.nav.logout}
                  className="h-9 w-9 text-zinc-500 hover:text-red-600"
                >
                  <LogOut size={18} />
                </Button>
              </div>
            ) : (
              <div className="flex items-center gap-2">
                <Link href="/login">
                  <Button size="sm" variant="ghost" className="font-semibold">
                    <span suppressHydrationWarning>{t.nav.login}</span>
                  </Button>
                </Link>
                <Link href="/register">
                  <Button size="sm" className="rounded-full px-5 bg-emerald-600 hover:bg-emerald-700 text-white border-0">
                    <span suppressHydrationWarning>{t.nav.signup}</span>
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
