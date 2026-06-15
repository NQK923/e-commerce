'use client';

import Link from "next/link";
import { usePathname } from "next/navigation";
import React from "react";
import { ShoppingBag, User, Search, Menu, LogOut, Store } from "lucide-react";
import { useAuth } from "../../store/auth-store";
import { useCart } from "../../store/cart-store";
import { cx } from "../../utils/cx";
import { Button } from "../ui/button";
import { useTranslation } from "../../providers/language-provider";
import { NotificationDropdown } from "../notification/notification-dropdown";

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
    ...(isAdmin ? [{ href: "/admin", label: t.nav.admin }] : []),
  ];

  if (pathname?.startsWith('/admin')) {
    return null;
  }

  return (
    <header className="sticky top-0 z-40 bg-white border-b border-zinc-100">
      <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-5">
        {/* Left: Logo & Desktop Nav */}
        <div className="flex items-center gap-10">
          <Link href="/" className="flex items-center gap-2 text-2xl font-bold tracking-tight text-black">
            ECOMX.
          </Link>

          <nav className="hidden items-center gap-8 text-sm font-medium md:flex">
            {/* Seller Channel Link */}
            <Link
              href="/seller/dashboard"
              className="flex items-center gap-1 font-semibold text-zinc-600 transition-colors hover:text-black"
            >
              <Store size={16} />
              <span suppressHydrationWarning>{t.nav.sellerChannel}</span>
            </Link>

            <div className="h-4 w-px bg-zinc-200 mx-1" />

            {navItems
              .filter((item) => (item.requiresAuth ? (mounted && Boolean(user)) : true))
              .map((item) => (
                <Link
                  key={item.href}
                  href={item.href}
                  className={cx(
                    "relative text-zinc-600 transition-colors hover:text-black group",
                    pathname === item.href && "text-black"
                  )}
                >
                  <span suppressHydrationWarning>{item.label}</span>
                  <span className={cx(
                    "absolute -bottom-1 left-0 h-[1.5px] bg-black transition-all duration-300",
                    pathname === item.href ? "w-full" : "w-0 group-hover:w-full"
                  )}></span>
                </Link>
              ))}
          </nav>
        </div>

        {/* Right: Actions */}
        <div className="flex items-center gap-5">
          <button className="hidden text-black transition-opacity hover:opacity-70 sm:block">
            <Search size={20} strokeWidth={1.5} />
          </button>

          <NotificationDropdown />

          <Link href="/cart" className="group relative text-black transition-opacity hover:opacity-70">
            <ShoppingBag size={20} strokeWidth={1.5} />
            {mounted && itemCount > 0 && (
              <span className="absolute -right-2 -top-2 flex h-4 w-4 items-center justify-center rounded-full bg-black text-[10px] font-bold text-white">
                {itemCount}
              </span>
            )}
          </Link>

          {/* Auth */}
          <div className="flex items-center gap-4 border-l border-zinc-200 pl-5">
            {mounted && user ? (
              <div className="flex items-center gap-4">
                <Link href="/profile" className="hidden flex items-center gap-2 text-sm font-medium text-black transition-opacity hover:opacity-70 sm:flex">
                  <User size={18} strokeWidth={1.5} />
                  <span suppressHydrationWarning>{user.displayName || t.nav.account}</span>
                </Link>
                <button
                  onClick={() => logout()}
                  title={t.nav.logout}
                  className="text-zinc-400 hover:text-red-600 transition-colors"
                >
                  <LogOut size={18} strokeWidth={1.5} />
                </button>
              </div>
            ) : (
              <div className="flex items-center gap-4">
                <Link href="/login" className="text-sm font-medium text-zinc-600 hover:text-black transition-colors">
                  <span suppressHydrationWarning>{t.nav.login}</span>
                </Link>
                <Link href="/register">
                  <Button size="sm" className="rounded-md bg-black text-white px-6 hover:bg-zinc-800">
                    <span suppressHydrationWarning>{t.nav.signup}</span>
                  </Button>
                </Link>
              </div>
            )}
          </div>

          {/* Mobile Menu Toggle */}
          <button className="block md:hidden text-black">
            <Menu size={24} strokeWidth={1.5} />
          </button>
        </div>
      </div>
    </header>
  );
};
