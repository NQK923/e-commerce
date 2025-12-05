'use client';

import React, { useState } from "react";
import { profileApi } from "@/src/api/profileApi";
import { useAuth } from "@/src/store/auth-store";
import { Button } from "@/src/components/ui/button";
import { Input } from "@/src/components/ui/input";
import { useToast } from "@/src/components/ui/toast-provider";
import Link from "next/link";

export default function AddressesPage() {
  const { user, setUserProfile } = useAuth();
  const { addToast } = useToast();
  const [adding, setAdding] = useState(false);
  const [loading, setLoading] = useState(false);
  
  const [form, setForm] = useState({
    label: "Nhà riêng",
    isDefault: false,
    fullName: user?.displayName || "",
    phoneNumber: "",
    line1: "",
    line2: "",
    city: "",
    state: "",
    postalCode: "",
    country: "Việt Nam",
  });

  const handleDelete = async (id: string) => {
    if (!confirm("Bạn có chắc muốn xóa địa chỉ này?")) return;
    try {
        await profileApi.deleteAddress(id);
        if (user) {
            const newAddresses = user.addresses?.filter(a => a.id !== id) || [];
            setUserProfile({ ...user, addresses: newAddresses });
        }
        addToast("Đã xóa địa chỉ", "success");
    } catch (e) {
        addToast("Xóa thất bại", "error");
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
        const newAddr = await profileApi.addAddress(form);
        if (user) {
            const current = user.addresses || [];
            let updated = [...current];
            if (form.isDefault) {
                updated = updated.map(a => ({ ...a, isDefault: false }));
            }
            updated.push(newAddr);
            setUserProfile({ ...user, addresses: updated });
        }
        setAdding(false);
        setForm({ ...form, line1: "", line2: "", city: "", state: "", postalCode: "", fullName: user?.displayName || "" });
        addToast("Đã thêm địa chỉ", "success");
    } catch (e) {
        addToast("Thêm địa chỉ thất bại", "error");
    } finally {
        setLoading(false);
    }
  };

  return (
     <div className="mx-auto max-w-3xl px-4 py-10 space-y-6">
        <div className="flex items-center justify-between">
            <h1 className="text-2xl font-bold text-zinc-900">Sổ địa chỉ</h1>
            <Link href="/profile">
                <Button variant="ghost">Quay lại</Button>
            </Link>
        </div>

        {/* List */}
        <div className="grid gap-4">
            {user?.addresses?.map((addr) => (
                <div key={addr.id} className="relative rounded-xl border border-zinc-200 p-4 shadow-sm bg-white">
                    <div className="flex justify-between items-start">
                        <div>
                            <div className="flex items-center gap-2">
                                <span className="font-semibold text-zinc-900">{addr.label}</span>
                                {addr.isDefault && <span className="rounded bg-emerald-100 px-2 py-0.5 text-xs font-medium text-emerald-700">Mặc định</span>}
                            </div>
                            <p className="text-sm text-zinc-900 mt-1 font-medium">{addr.address.fullName} - {addr.address.phoneNumber}</p>
                            <p className="text-sm text-zinc-500">{addr.address.line1}, {addr.address.city}, {addr.address.country}</p>
                        </div>
                        <Button variant="ghost" className="text-red-600 hover:bg-red-50 hover:text-red-700 h-8 px-3" onClick={() => handleDelete(addr.id)}>Xóa</Button>
                    </div>
                </div>
            ))}
            {(!user?.addresses || user.addresses.length === 0) && (
                <div className="flex flex-col items-center justify-center rounded-xl border border-dashed border-zinc-300 py-10">
                     <p className="text-zinc-500 mb-4">Bạn chưa lưu địa chỉ nào.</p>
                     {!adding && <Button onClick={() => setAdding(true)}>Thêm địa chỉ ngay</Button>}
                </div>
            )}
        </div>

        {/* Add Form */}
        {!adding && (user?.addresses && user.addresses.length > 0) && (
            <Button onClick={() => setAdding(true)} className="w-full bg-emerald-600 hover:bg-emerald-700">Thêm địa chỉ mới</Button>
        )}

        {adding && (
            <form onSubmit={handleSubmit} className="rounded-xl border border-zinc-200 bg-white p-6 space-y-4 shadow-sm">
                <h3 className="font-semibold text-lg">Thêm địa chỉ mới</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <Input label="Nhãn (VD: Nhà riêng)" value={form.label} onChange={e => setForm({...form, label: e.target.value})} required />
                    <Input label="Họ tên người nhận" value={form.fullName} onChange={e => setForm({...form, fullName: e.target.value})} required />
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <Input label="Số điện thoại" value={form.phoneNumber} onChange={e => setForm({...form, phoneNumber: e.target.value})} required />
                    <Input label="Quốc gia" value={form.country} onChange={e => setForm({...form, country: e.target.value})} required />
                </div>
                <Input label="Địa chỉ (Số nhà, tên đường, phường/xã)" value={form.line1} onChange={e => setForm({...form, line1: e.target.value})} required />
                <Input label="Địa chỉ 2 (Tòa nhà, số phòng - Tùy chọn)" value={form.line2} onChange={e => setForm({...form, line2: e.target.value})} />
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <Input label="Thành phố/Tỉnh" value={form.city} onChange={e => setForm({...form, city: e.target.value})} required />
                    <Input label="Quận/Huyện" value={form.state} onChange={e => setForm({...form, state: e.target.value})} />
                    <Input label="Mã bưu chính" value={form.postalCode} onChange={e => setForm({...form, postalCode: e.target.value})} />
                </div>
                <label className="flex items-center gap-2 text-sm cursor-pointer select-none">
                    <input type="checkbox" className="h-4 w-4 rounded border-zinc-300 text-emerald-600 focus:ring-emerald-500" checked={form.isDefault} onChange={e => setForm({...form, isDefault: e.target.checked})} />
                    Đặt làm địa chỉ mặc định
                </label>
                <div className="flex gap-3 pt-2">
                    <Button type="submit" disabled={loading} className="bg-emerald-600 hover:bg-emerald-700 flex-1">
                         {loading ? "Đang lưu..." : "Lưu địa chỉ"}
                    </Button>
                    <Button type="button" variant="ghost" onClick={() => setAdding(false)} className="flex-1">Hủy bỏ</Button>
                </div>
            </form>
        )}
     </div>
  );
}
