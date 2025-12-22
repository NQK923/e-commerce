'use client';

import React, { useState } from "react";
import { profileApi } from "@/src/api/profileApi";
import { useAuth } from "@/src/store/auth-store";
import { Button } from "@/src/components/ui/button";
import { Input } from "@/src/components/ui/input";
import { useToast } from "@/src/components/ui/toast-provider";
import Link from "next/link";
import { useTranslation } from "@/src/providers/language-provider";

export default function AddressesPage() {
  const { user, setUserProfile } = useAuth();
  const { addToast } = useToast();
  const { t } = useTranslation();
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
    if (!confirm(t.profile.addresses.confirm_delete)) return;
    try {
        await profileApi.deleteAddress(id);
        if (user) {
            const newAddresses = user.addresses?.filter(a => a.id !== id) || [];
            setUserProfile({ ...user, addresses: newAddresses });
        }
        addToast(t.profile.addresses.delete_success, "success");
    } catch {
        addToast(t.profile.addresses.delete_failed, "error");
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
        addToast(t.profile.addresses.add_success, "success");
    } catch {
        addToast(t.profile.addresses.add_failed, "error");
    } finally {
        setLoading(false);
    }
  };

  return (
     <div className="mx-auto max-w-3xl px-4 py-10 space-y-6">
        <div className="flex items-center justify-between">
            <h1 className="text-2xl font-bold text-zinc-900">{t.profile.addresses.title}</h1>
            <Link href="/profile">
                <Button variant="ghost">{t.profile.addresses.back}</Button>
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
                                {addr.isDefault && <span className="rounded bg-emerald-100 px-2 py-0.5 text-xs font-medium text-emerald-700">{t.profile.addresses.default_badge}</span>}
                            </div>
                            <p className="text-sm text-zinc-900 mt-1 font-medium">{addr.address.fullName} - {addr.address.phoneNumber}</p>
                            <p className="text-sm text-zinc-500">{addr.address.line1}, {addr.address.city}, {addr.address.country}</p>
                        </div>
                        <Button variant="ghost" className="text-red-600 hover:bg-red-50 hover:text-red-700 h-8 px-3" onClick={() => handleDelete(addr.id)}>{t.profile.addresses.delete}</Button>
                    </div>
                </div>
            ))}
            {(!user?.addresses || user.addresses.length === 0) && (
                <div className="flex flex-col items-center justify-center rounded-xl border border-dashed border-zinc-300 py-10">
                     <p className="text-zinc-500 mb-4">{t.profile.addresses.empty}</p>
                     {!adding && <Button onClick={() => setAdding(true)}>{t.profile.addresses.add_now}</Button>}
                </div>
            )}
        </div>

        {/* Add Form */}
        {!adding && (user?.addresses && user.addresses.length > 0) && (
            <Button onClick={() => setAdding(true)} className="w-full bg-emerald-600 hover:bg-emerald-700">{t.profile.addresses.add_new}</Button>
        )}

        {adding && (
            <form onSubmit={handleSubmit} className="rounded-xl border border-zinc-200 bg-white p-6 space-y-4 shadow-sm">
                <h3 className="font-semibold text-lg">{t.profile.addresses.add_title}</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <Input label={t.profile.addresses.label_label} value={form.label} onChange={e => setForm({...form, label: e.target.value})} required />
                    <Input label={t.profile.addresses.fullname_label} value={form.fullName} onChange={e => setForm({...form, fullName: e.target.value})} required />
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <Input label={t.profile.addresses.phone_label} value={form.phoneNumber} onChange={e => setForm({...form, phoneNumber: e.target.value})} required />
                    <Input label={t.profile.addresses.country_label} value={form.country} onChange={e => setForm({...form, country: e.target.value})} required />
                </div>
                <Input label={t.profile.addresses.line1_label} value={form.line1} onChange={e => setForm({...form, line1: e.target.value})} required />
                <Input label={t.profile.addresses.line2_label} value={form.line2} onChange={e => setForm({...form, line2: e.target.value})} />
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <Input label={t.profile.addresses.city_label} value={form.city} onChange={e => setForm({...form, city: e.target.value})} required />
                    <Input label={t.profile.addresses.state_label} value={form.state} onChange={e => setForm({...form, state: e.target.value})} />
                    <Input label={t.profile.addresses.postal_code_label} value={form.postalCode} onChange={e => setForm({...form, postalCode: e.target.value})} />
                </div>
                <label className="flex items-center gap-2 text-sm cursor-pointer select-none">
                    <input type="checkbox" className="h-4 w-4 rounded border-zinc-300 text-emerald-600 focus:ring-emerald-500" checked={form.isDefault} onChange={e => setForm({...form, isDefault: e.target.checked})} />
                    {t.profile.addresses.set_default}
                </label>
                <div className="flex gap-3 pt-2">
                    <Button type="submit" disabled={loading} className="bg-emerald-600 hover:bg-emerald-700 flex-1">
                         {loading ? t.profile.saving : t.profile.addresses.save_address}
                    </Button>
                    <Button type="button" variant="ghost" onClick={() => setAdding(false)} className="flex-1">{t.profile.cancel}</Button>
                </div>
            </form>
        )}
     </div>
  );
}
