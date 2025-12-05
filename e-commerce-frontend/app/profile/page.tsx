'use client';

import React, { useEffect, useState } from "react";
import Link from "next/link";
import { profileApi } from "@/src/api/profileApi";
import { Button } from "@/src/components/ui/button";
import { Input } from "@/src/components/ui/input";
import { Spinner } from "@/src/components/ui/spinner";
import { useRequireAuth } from "@/src/hooks/use-require-auth";
import { useAuth } from "@/src/store/auth-store";
import { useToast } from "@/src/components/ui/toast-provider";
import { uploadToBucket } from "@/src/lib/storage";

export default function ProfilePage() {
  const { user, setUserProfile } = useAuth();
  const { isAuthenticated, initializing } = useRequireAuth();
  const { addToast } = useToast();

  const [displayName, setDisplayName] = useState(user?.displayName ?? "");
  const [avatarUrl, setAvatarUrl] = useState(user?.avatarUrl ?? "");
  const [uploading, setUploading] = useState(false);
  const [loading, setLoading] = useState(false);
  const [isEditing, setIsEditing] = useState(false);

  useEffect(() => {
    if (user) {
      setDisplayName(user.displayName || "");
      setAvatarUrl(user.avatarUrl || "");
    }
  }, [user]);

  if (initializing || !isAuthenticated) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center gap-3 text-sm text-zinc-600">
        <Spinner />
        Đang tải thông tin...
      </div>
    );
  }

  if (!user) {
    return (
      <div className="mx-auto flex max-w-3xl flex-col items-center gap-3 px-4 py-10 text-center">
        <p className="text-lg font-semibold text-black">Không thể tải thông tin tài khoản.</p>
      </div>
    );
  }

  const handleUpdate = async (event: React.FormEvent) => {
    event.preventDefault();
    setLoading(true);
    try {
      const updated = await profileApi.update({ displayName, avatarUrl });
      setUserProfile(updated);
      addToast("Cập nhật thành công", "success");
      setIsEditing(false);
    } catch (error) {
      const message = error instanceof Error ? error.message : "Cập nhật thất bại";
      addToast(message, "error");
    } finally {
      setLoading(false);
    }
  };

  const handleAvatarUpload = async (file?: File) => {
    if (!file) return;
    setUploading(true);
    try {
      const publicUrl = await uploadToBucket("Avatars", file);
      setAvatarUrl(publicUrl);
      addToast("Đã tải ảnh lên", "success");
    } catch (error) {
      const message = error instanceof Error ? error.message : "Tải ảnh thất bại";
      addToast(message, "error");
    } finally {
      setUploading(false);
    }
  };



  return (
    <div className="mx-auto max-w-5xl px-4 py-10 space-y-8">
      {/* Header Section */}
      <div className="flex items-start justify-between border-b border-zinc-200 pb-6">
        <div>
          <h1 className="text-3xl font-bold text-zinc-900">Hồ sơ của tôi</h1>
          <p className="text-sm text-zinc-600 mt-1">Quản lý thông tin tài khoản và đơn hàng.</p>
        </div>
      </div>

      <div className="grid gap-8 md:grid-cols-[1fr_2fr]">
        {/* Left Column: Profile Card */}
        <div className="space-y-6">
          <div className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm">
            <div className="flex flex-col items-center gap-4 text-center">
              <div className="relative h-24 w-24 overflow-hidden rounded-full border-2 border-emerald-50 bg-zinc-100">
                {avatarUrl ? (
                  <img src={avatarUrl} alt="Avatar" className="h-full w-full object-cover" />
                ) : (
                  <div className="flex h-full w-full items-center justify-center text-2xl font-bold text-zinc-300">
                    {displayName.charAt(0).toUpperCase() || "U"}
                  </div>
                )}
                {isEditing && (
                  <div className="absolute inset-0 flex items-center justify-center bg-black/40 opacity-0 hover:opacity-100 transition-opacity cursor-pointer">
                     <label className="cursor-pointer p-2 text-xs text-white">
                        Upload
                        <input
                          type="file"
                          className="hidden"
                          accept="image/*"
                          onChange={(e) => handleAvatarUpload(e.target.files?.[0])}
                          disabled={uploading}
                        />
                     </label>
                  </div>
                )}
              </div>
              
              <div>
                <h2 className="text-xl font-bold text-zinc-900">{user.displayName}</h2>
                <p className="text-sm text-zinc-500">{user.email}</p>
                {user.roles.includes("SELLER") && (
                  <span className="mt-2 inline-block rounded-full bg-emerald-100 px-2 py-0.5 text-xs font-medium text-emerald-700">
                    Người bán
                  </span>
                )}
              </div>

              {!isEditing ? (
                <Button variant="outline" className="w-full" onClick={() => setIsEditing(true)}>
                  Chỉnh sửa hồ sơ
                </Button>
              ) : (
                 <Button variant="ghost" className="w-full text-zinc-500" onClick={() => setIsEditing(false)}>
                  Hủy bỏ
                </Button>
              )}
            </div>

            {isEditing && (
              <form className="mt-6 space-y-4 border-t border-zinc-100 pt-4" onSubmit={handleUpdate}>
                 <Input
                    label="Tên hiển thị"
                    value={displayName}
                    onChange={(e) => setDisplayName(e.target.value)}
                    required
                  />
                 <Button type="submit" disabled={loading} className="w-full bg-emerald-600 hover:bg-emerald-700">
                    {loading ? "Đang lưu..." : "Lưu thay đổi"}
                  </Button>
              </form>
            )}
          </div>
        </div>

        {/* Right Column: Quick Links / Dashboard */}
        <div className="grid gap-4 sm:grid-cols-2">
            <Link href="/orders" className="group flex flex-col justify-between rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm transition-all hover:border-emerald-500 hover:shadow-md">
               <div>
                 <div className="mb-4 inline-flex h-10 w-10 items-center justify-center rounded-full bg-blue-50 text-blue-600 group-hover:bg-blue-100">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z"/><path d="M3 6h18"/><path d="M16 10a4 4 0 0 1-8 0"/></svg>
                 </div>
                 <h3 className="font-semibold text-zinc-900">Đơn hàng của tôi</h3>
                 <p className="text-sm text-zinc-500 mt-1">Xem lịch sử mua hàng và trạng thái đơn hàng.</p>
               </div>
               <span className="mt-4 text-sm font-medium text-emerald-600 group-hover:underline">Xem chi tiết &rarr;</span>
            </Link>

            <Link href="/cart" className="group flex flex-col justify-between rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm transition-all hover:border-emerald-500 hover:shadow-md">
               <div>
                 <div className="mb-4 inline-flex h-10 w-10 items-center justify-center rounded-full bg-amber-50 text-amber-600 group-hover:bg-amber-100">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="8" cy="21" r="1"/><circle cx="19" cy="21" r="1"/><path d="M2.05 2.05h2l2.66 12.42a2 2 0 0 0 2 1.58h9.78a2 2 0 0 0 1.95-1.57l1.65-7.43H5.12"/></svg>
                 </div>
                 <h3 className="font-semibold text-zinc-900">Giỏ hàng</h3>
                 <p className="text-sm text-zinc-500 mt-1">Các sản phẩm bạn đã chọn mua.</p>
               </div>
               <span className="mt-4 text-sm font-medium text-emerald-600 group-hover:underline">Đến giỏ hàng &rarr;</span>
            </Link>

            <Link href="/profile/addresses" className="group flex flex-col justify-between rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm transition-all hover:border-emerald-500 hover:shadow-md">
               <div>
                 <div className="mb-4 inline-flex h-10 w-10 items-center justify-center rounded-full bg-purple-50 text-purple-600 group-hover:bg-purple-100">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z"/><circle cx="12" cy="10" r="3"/></svg>
                 </div>
                 <h3 className="font-semibold text-zinc-900">Sổ địa chỉ</h3>
                 <p className="text-sm text-zinc-500 mt-1">Quản lý địa chỉ giao hàng.</p>
               </div>
               <span className="mt-4 text-sm font-medium text-emerald-600 group-hover:underline">Xem chi tiết &rarr;</span>
            </Link>

             {/* Seller Link */}
             <Link href="/seller/dashboard" className="group flex flex-col justify-between rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm transition-all hover:border-emerald-500 hover:shadow-md">
               <div>
                 <div className="mb-4 inline-flex h-10 w-10 items-center justify-center rounded-full bg-emerald-50 text-emerald-600 group-hover:bg-emerald-100">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                 </div>
                 <h3 className="font-semibold text-zinc-900">Kênh người bán</h3>
                 <p className="text-sm text-zinc-500 mt-1">Quản lý sản phẩm và gian hàng của bạn.</p>
               </div>
               <span className="mt-4 text-sm font-medium text-emerald-600 group-hover:underline">Truy cập &rarr;</span>
            </Link>
        </div>
      </div>
    </div>
  );
}
