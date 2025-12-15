'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import {
  LayoutDashboard,
  Users,
  Package,
  ShoppingCart,
  Settings,
  LogOut,
  Menu,
  Bell,
  Search,
  Store,
  ShieldCheck,
  BarChart3,
  Flag,
  Zap
} from 'lucide-react';
import { Button } from '@/src/components/ui/button';
import { useAuth } from '@/src/store/auth-store';

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const [sidebarOpen, setSidebarOpen] = React.useState(true);
  const pathname = usePathname();
  const router = useRouter();
  const { logout } = useAuth();

  const handleLogout = async () => {
    await logout();
    router.push('/login');
  };

  const navItems = [
    { label: 'Tổng quan', icon: LayoutDashboard, href: '/admin' },
    { label: 'Người dùng', icon: Users, href: '/admin/users' },
    { label: 'Sản phẩm', icon: Package, href: '/admin/products' },
    { label: 'Đơn hàng', icon: ShoppingCart, href: '/admin/orders' },
    { label: 'Flash Sales', icon: Zap, href: '/admin/flash-sales' },
    { label: 'Người bán', icon: Store, href: '/admin/sellers' },
    { label: 'Thống kê', icon: BarChart3, href: '/admin/statistics' },
    { label: 'Báo cáo', icon: Flag, href: '/admin/reports' },
    { label: 'Cài đặt', icon: Settings, href: '/admin/settings' },
  ];

  return (
    <div className="min-h-screen bg-zinc-50 flex">
      {/* Sidebar */}
      <aside
        className={`${
          sidebarOpen ? 'w-64' : 'w-20'
        } bg-white border-r border-zinc-200 text-zinc-600 transition-all duration-300 ease-in-out flex flex-col fixed h-full z-30 md:relative`}
      >
        <div className="h-16 flex items-center justify-center border-b border-zinc-200">
            {sidebarOpen ? (
                <div className="flex items-center gap-2 text-zinc-900 font-bold text-xl tracking-tight">
                    <ShieldCheck className="text-emerald-600" />
                    <span>Admin<span className="text-emerald-600">Panel</span></span>
                </div>
            ) : (
                <ShieldCheck className="text-emerald-600" />
            )}
        </div>

        <nav className="flex-1 py-6 space-y-1 px-3">
          {navItems.map((item) => {
            const isActive = pathname === item.href;
            return (
              <Link
                key={item.href}
                href={item.href}
                className={`flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors ${
                  isActive
                    ? 'bg-emerald-50 text-emerald-700 font-medium border border-emerald-100'
                    : 'hover:bg-zinc-100 hover:text-zinc-900'
                }`}
              >
                <item.icon size={20} className={isActive ? "text-emerald-600" : "text-zinc-500"} />
                {sidebarOpen && <span>{item.label}</span>}
              </Link>
            );
          })}
        </nav>

        <div className="p-4 border-t border-zinc-200">
          <button
            onClick={handleLogout}
            className="flex items-center gap-3 w-full px-3 py-2 rounded-lg text-red-500 hover:bg-red-50 hover:text-red-600 transition-colors"
          >
            <LogOut size={20} />
            {sidebarOpen && <span className="font-medium">Đăng xuất</span>}
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 flex flex-col min-w-0 overflow-hidden">
        {/* Top Header */}
        <header className="h-16 bg-white border-b border-zinc-200 flex items-center justify-between px-6 sticky top-0 z-20">
          <div className="flex items-center gap-4">
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setSidebarOpen(!sidebarOpen)}
              className="text-zinc-500 hover:text-zinc-900"
            >
              <Menu size={20} />
            </Button>
            <div className="relative hidden md:block">
              <Search className="absolute left-2.5 top-1/2 -translate-y-1/2 text-zinc-400" size={16} />
              <input
                type="text"
                placeholder="Search..."
                className="pl-9 pr-4 py-1.5 rounded-full border border-zinc-200 bg-zinc-50 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500 w-64"
              />
            </div>
          </div>

          <div className="flex items-center gap-4">
            <button className="relative p-2 text-zinc-500 hover:bg-zinc-100 rounded-full transition-colors">
              <Bell size={20} />
              <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-red-500 rounded-full border border-white"></span>
            </button>
            <div className="h-8 w-8 rounded-full bg-emerald-100 border border-emerald-200 flex items-center justify-center text-emerald-700 font-semibold text-sm">
              AD
            </div>
          </div>
        </header>

        {/* Page Content */}
        <div className="flex-1 overflow-auto p-6">
          <div className="max-w-7xl mx-auto">
            {children}
          </div>
        </div>
      </main>
    </div>
  );
}
