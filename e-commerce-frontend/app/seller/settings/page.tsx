'use client';

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import { useRequireAuth } from "@/src/hooks/use-require-auth";
import { userApi } from "@/src/api/userApi";
import { Button } from "@/src/components/ui/button";
import { Input } from "@/src/components/ui/input";
import { Spinner } from "@/src/components/ui/spinner";
import { useToast } from "@/src/components/ui/toast-provider";
import { Save, Upload } from "lucide-react";

export default function SellerSettingsPage() {
  const { user, loading: authLoading } = useRequireAuth("/login");
  const router = useRouter();
  const { addToast } = useToast();
  
  const [formData, setFormData] = useState({
    displayName: "",
    shopDescription: "",
    shopBannerUrl: "",
    avatarUrl: ""
  });
  const [initialized, setInitialized] = useState(false);
  const [saving, setSaving] = useState(false);

  React.useEffect(() => {
    if (user && !initialized) {
        setFormData({
            displayName: user.displayName || "",
            shopDescription: user.shopDescription || "",
            shopBannerUrl: user.shopBannerUrl || "",
            avatarUrl: user.avatarUrl || ""
        });
        setInitialized(true);
    }
  }, [user, initialized]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
        await userApi.updateProfile(formData);
        addToast("Shop settings updated successfully!", "success");
        // Force reload or re-fetch user might be needed if useRequireAuth doesn't auto-update
        // Typically useRequireAuth uses a store. We might need to update the store.
        // For now, reload page is safest to refresh global state if store doesn't listen to updates.
        window.location.reload(); 
    } catch (error) {
        console.error(error);
        addToast("Failed to update settings.", "error");
    } finally {
        setSaving(false);
    }
  };

  if (authLoading || !user) {
    return <div className="p-10 flex justify-center"><Spinner /></div>;
  }

  return (
    <div className="mx-auto max-w-3xl px-4 py-8 space-y-8 bg-zinc-50 min-h-screen">
      <div className="flex items-center justify-between">
         <h1 className="text-2xl font-bold text-zinc-900">Shop Settings</h1>
         <Button variant="outline" onClick={() => router.back()}>Cancel</Button>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6 bg-white p-6 rounded-xl shadow-sm border border-zinc-200">
        <div className="space-y-4">
            <div>
                <label className="block text-sm font-medium text-zinc-700 mb-1">Shop Name (Display Name)</label>
                <Input 
                    name="displayName"
                    value={formData.displayName}
                    onChange={handleChange}
                    placeholder="My Awesome Shop"
                    required
                />
            </div>

            <div>
                <label className="block text-sm font-medium text-zinc-700 mb-1">Description</label>
                <textarea 
                    name="shopDescription"
                    value={formData.shopDescription}
                    onChange={handleChange}
                    rows={4}
                    className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm focus:border-emerald-500 focus:outline-none focus:ring-1 focus:ring-emerald-500"
                    placeholder="Tell us about your shop..."
                />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                    <label className="block text-sm font-medium text-zinc-700 mb-1">Avatar URL</label>
                    <div className="flex gap-2">
                        <Input 
                            name="avatarUrl"
                            value={formData.avatarUrl}
                            onChange={handleChange}
                            placeholder="https://..."
                        />
                        {formData.avatarUrl && (
                             <div className="h-10 w-10 shrink-0 overflow-hidden rounded-full border border-zinc-200">
                                {/* eslint-disable-next-line @next/next/no-img-element */}
                                <img src={formData.avatarUrl} alt="Preview" className="h-full w-full object-cover" />
                             </div>
                        )}
                    </div>
                </div>

                <div>
                    <label className="block text-sm font-medium text-zinc-700 mb-1">Banner URL</label>
                    <div className="space-y-2">
                        <Input 
                            name="shopBannerUrl"
                            value={formData.shopBannerUrl}
                            onChange={handleChange}
                            placeholder="https://..."
                        />
                         {formData.shopBannerUrl && (
                             <div className="h-20 w-full overflow-hidden rounded-md border border-zinc-200">
                                {/* eslint-disable-next-line @next/next/no-img-element */}
                                <img src={formData.shopBannerUrl} alt="Preview" className="h-full w-full object-cover" />
                             </div>
                        )}
                    </div>
                </div>
            </div>
        </div>

        <div className="pt-4 border-t border-zinc-100 flex justify-end">
             <Button type="submit" disabled={saving} className="bg-emerald-600 hover:bg-emerald-700">
                {saving ? <Spinner size="sm" className="mr-2" /> : <Save size={18} className="mr-2" />}
                Save Changes
             </Button>
        </div>
      </form>
    </div>
  );
}
