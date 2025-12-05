'use client';

import Link from "next/link";
import React, { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { ArrowRight, CheckCircle2, ShieldCheck, Sparkles, Store, MessageCircle } from "lucide-react";
import { Button } from "@/src/components/ui/button";
import { Input } from "@/src/components/ui/input";
import { useToast } from "@/src/components/ui/toast-provider";
import { useTranslation } from "@/src/providers/language-provider";
import { useRequireAuth } from "@/src/hooks/use-require-auth";
import { Spinner } from "@/src/components/ui/spinner";
import { sellerApi } from "@/src/api/sellerApi";
import { ApiError } from "@/src/lib/api-client";
import { uploadToBucket } from "@/src/lib/storage";
import { SellerApplication } from "@/src/types/seller";

type SellerForm = {
  storeName: string;
  email: string;
  phone: string;
  category: string;
  description: string;
  accept: boolean;
};

export default function SellerRegisterPage() {
  const { t } = useTranslation();
  const { addToast } = useToast();
  const { user, initializing } = useRequireAuth();
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [loadingExisting, setLoadingExisting] = useState(false);
  const [avatarFile, setAvatarFile] = useState<File | null>(null);
  const [coverFile, setCoverFile] = useState<File | null>(null);
  const [existingApplication, setExistingApplication] = useState<SellerApplication | null>(null);
  const formDisabled = existingApplication?.status === "PENDING" || existingApplication?.status === "APPROVED";
  const [form, setForm] = useState<SellerForm>({
    storeName: "",
    email: "",
    phone: "",
    category: t.seller.form.categories[0],
    description: "",
    accept: true,
  });

  useEffect(() => {
    if (user) {
      setForm((prev) => ({
        ...prev,
        email: prev.email || user.email,
        storeName: prev.storeName || user.displayName || user.email,
      }));
      const load = async () => {
        setLoadingExisting(true);
        try {
          const app = await sellerApi.myApplication();
          setExistingApplication(app);
        } catch {
          setExistingApplication(null);
        } finally {
          setLoadingExisting(false);
        }
      };
      void load();
    }
  }, [user]);

  useEffect(() => {
    if (!existingApplication) return;
    if (existingApplication.status === "APPROVED") {
      router.replace("/seller/dashboard");
    }
  }, [existingApplication, router]);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!form.storeName || !form.email || !form.phone || !form.description || !form.accept) {
      addToast(t.seller.missing_fields, "error");
      return;
    }
    if (existingApplication?.status === "PENDING") {
      addToast("Đã gửi yêu cầu, vui lòng chờ duyệt", "info");
      return;
    }
    if (existingApplication?.status === "APPROVED") {
      router.replace("/seller/dashboard");
      return;
    }
    setLoading(true);
    try {
      const [avatarUrl, coverUrl] = await Promise.all([
        avatarFile ? uploadToBucket("seller", avatarFile) : Promise.resolve(undefined),
        coverFile ? uploadToBucket("seller", coverFile) : Promise.resolve(undefined),
      ]);
      const created = await sellerApi.submitApplication({
        userId: user?.id,
        storeName: form.storeName,
        email: form.email,
        phone: form.phone,
        category: form.category,
        description: form.description,
        avatarUrl,
        coverUrl,
        acceptedTerms: form.accept,
      });
      setExistingApplication(created);
      addToast(t.seller.success, "success");
    } catch (error) {
      const message = error instanceof ApiError ? error.message : t.common.error;
      addToast(message, "error");
    } finally {
      setLoading(false);
    }
  };

  if (initializing || !user) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center">
        <Spinner />
      </div>
    );
  }

  return (
    <div className="bg-gradient-to-b from-emerald-50 via-white to-white">
      <section className="relative overflow-hidden border-b border-emerald-100 bg-emerald-700 text-white">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_20%_20%,#34d39933,transparent_35%),radial-gradient(circle_at_80%_0%,#a7f3d033,transparent_30%)]" />
        <div className="relative mx-auto flex max-w-6xl flex-col gap-6 px-4 py-16 sm:flex-row sm:items-center sm:justify-between sm:py-20">
          <div className="space-y-4 sm:max-w-xl">
            <div className="inline-flex items-center gap-2 rounded-full bg-white/15 px-3 py-1 text-sm font-medium backdrop-blur">
              <Sparkles size={14} className="text-yellow-300" />
              {t.nav.sellerChannel}
            </div>
            <h1 className="text-4xl font-extrabold leading-tight sm:text-5xl">
              {t.seller.title}
            </h1>
            <p className="text-lg text-emerald-100">{t.seller.subtitle}</p>
            <div className="flex flex-wrap items-center gap-3">
              <Button size="lg" className="gap-2 bg-white text-emerald-700 hover:bg-emerald-50 border-0" onClick={() => document.getElementById("seller-form")?.scrollIntoView({ behavior: "smooth" })}>
                <Store size={18} />
                {t.seller.cta}
              </Button>
              <div className="flex items-center gap-2 text-sm text-emerald-100">
                <ShieldCheck size={16} />
                {t.home.features.payment_desc}
              </div>
            </div>
            <div className="flex flex-wrap gap-4">
              {t.seller.highlights.map((item, idx) => (
                <div key={idx} className="flex items-center gap-2 rounded-lg bg-white/10 px-3 py-2 text-sm">
                  <CheckCircle2 size={16} className="text-yellow-300" />
                  <div>
                    <div className="font-semibold">{item.title}</div>
                    <div className="text-emerald-100">{item.desc}</div>
                  </div>
                </div>
              ))}
            </div>
          </div>
          <div className="hidden rounded-2xl border border-white/20 bg-white/10 p-6 shadow-xl backdrop-blur sm:block sm:w-80">
            <div className="flex items-center gap-3 text-sm font-semibold text-white">
              <MessageCircle size={18} />
              <span>Seller Success</span>
            </div>
            <p className="mt-3 text-sm text-emerald-50">
              {t.home.hero_desc}
            </p>
            <ul className="mt-4 space-y-2 text-sm text-emerald-50">
              <li className="flex items-center gap-2"><ArrowRight size={14} />Onboarding within 24h</li>
              <li className="flex items-center gap-2"><ArrowRight size={14} />Account health guidance</li>
              <li className="flex items-center gap-2"><ArrowRight size={14} />Chat & logistics setup</li>
            </ul>
          </div>
        </div>
      </section>

      <section id="seller-form" className="mx-auto grid max-w-6xl grid-cols-1 gap-8 px-4 py-12 lg:grid-cols-3">
        <div className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm lg:col-span-2">
          {loadingExisting ? (
            <div className="mb-4 flex items-center gap-3 text-sm text-zinc-600">
              <Spinner />
              Đang kiểm tra trạng thái đăng ký...
            </div>
          ) : existingApplication ? (
            <div className="mb-4 rounded-xl border border-emerald-100 bg-emerald-50 px-4 py-3 text-sm text-emerald-800">
              <div className="flex items-center gap-2 font-semibold">
                <ShieldCheck size={16} /> Trạng thái: {existingApplication.status}
              </div>
              <div className="mt-1 text-emerald-700">
                {existingApplication.status === "PENDING" && "Bạn đã gửi yêu cầu. Vui lòng chờ admin duyệt."}
                {existingApplication.status === "APPROVED" && "Yêu cầu đã được chấp thuận. Chuyển sang dashboard seller."}
                {existingApplication.status === "REJECTED" && "Yêu cầu bị từ chối. Bạn có thể gửi lại thông tin cập nhật."}
              </div>
            </div>
          ) : null}

          <div className="mb-6 flex items-center justify-between">
            <div>
              <p className="text-sm font-semibold text-emerald-600">{t.nav.sellerChannel}</p>
              <h2 className="text-2xl font-bold text-zinc-900">{t.seller.title}</h2>
              <p className="text-sm text-zinc-500">{t.seller.subtitle}</p>
            </div>
            <div className="hidden rounded-full bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-700 sm:block">
              <Sparkles className="mr-1 inline-block" size={14} /> 24h review
            </div>
          </div>

          <form className="space-y-4" onSubmit={handleSubmit}>
            <div className="grid gap-4 md:grid-cols-2">
              <Input
                label={t.seller.form.store_name}
                required
                disabled={formDisabled}
                value={form.storeName}
                onChange={(e) => setForm((prev) => ({ ...prev, storeName: e.target.value }))}
              />
              <Input
                label={t.seller.form.contact_email}
                type="email"
                required
                disabled={formDisabled}
                value={form.email}
                onChange={(e) => setForm((prev) => ({ ...prev, email: e.target.value }))}
              />
              <Input
                label={t.seller.form.phone}
                required
                disabled={formDisabled}
                value={form.phone}
                onChange={(e) => setForm((prev) => ({ ...prev, phone: e.target.value }))}
              />
              <div className="flex flex-col gap-2">
                <label className="text-sm font-medium text-zinc-700">{t.seller.form.category}</label>
                <select
                  className="h-11 rounded-lg border border-zinc-200 bg-white px-3 text-sm text-zinc-800 outline-none transition focus:border-emerald-400 focus:ring-2 focus:ring-emerald-100"
                  disabled={formDisabled}
                  value={form.category}
                  onChange={(e) => setForm((prev) => ({ ...prev, category: e.target.value }))}
                >
                  {t.seller.form.categories.map((cat) => (
                    <option key={cat} value={cat}>
                      {cat}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div className="flex flex-col gap-2">
              <label className="text-sm font-medium text-zinc-700">{t.seller.form.description}</label>
              <textarea
                className="min-h-[110px] rounded-lg border border-zinc-200 bg-white px-3 py-2 text-sm text-zinc-800 outline-none transition focus:border-emerald-400 focus:ring-2 focus:ring-emerald-100"
                disabled={formDisabled}
                value={form.description}
                onChange={(e) => setForm((prev) => ({ ...prev, description: e.target.value }))}
                placeholder="List your main products, expected monthly orders, logistics needs..."
              />
            </div>

            <div className="grid gap-4 md:grid-cols-2">
              <div className="flex flex-col gap-2 text-sm text-zinc-700">
                <span className="font-medium">Store avatar (optional)</span>
                <input
                  type="file"
                  accept="image/*"
                  disabled={formDisabled}
                  onChange={(e) => setAvatarFile(e.target.files?.[0] ?? null)}
                  className="h-11 rounded-lg border border-zinc-200 bg-white px-3 py-2 text-sm text-zinc-800 outline-none transition focus:border-emerald-400 focus:ring-2 focus:ring-emerald-100"
                />
                {avatarFile && <span className="text-xs text-zinc-500 truncate">{avatarFile.name}</span>}
              </div>
              <div className="flex flex-col gap-2 text-sm text-zinc-700">
                <span className="font-medium">Cover image (optional)</span>
                <input
                  type="file"
                  accept="image/*"
                  disabled={formDisabled}
                  onChange={(e) => setCoverFile(e.target.files?.[0] ?? null)}
                  className="h-11 rounded-lg border border-zinc-200 bg-white px-3 py-2 text-sm text-zinc-800 outline-none transition focus:border-emerald-400 focus:ring-2 focus:ring-emerald-100"
                />
                {coverFile && <span className="text-xs text-zinc-500 truncate">{coverFile.name}</span>}
              </div>
            </div>

            <label className="flex items-start gap-3 text-sm text-zinc-700">
              <input
                type="checkbox"
                className="mt-1 h-4 w-4 accent-emerald-600"
                checked={form.accept}
                disabled={formDisabled}
                onChange={(e) => setForm((prev) => ({ ...prev, accept: e.target.checked }))}
              />
              <span>{t.seller.form.accept}</span>
            </label>

            <Button type="submit" disabled={loading || formDisabled} className="bg-emerald-600 hover:bg-emerald-700">
              {loading ? t.seller.form.loading : formDisabled ? "Đã gửi" : t.seller.cta}
            </Button>
          </form>
        </div>

        <div className="space-y-4">
          {existingApplication && (
            <div className="rounded-2xl border border-emerald-100 bg-white p-5 shadow-sm">
              <h3 className="text-lg font-semibold text-zinc-900 flex items-center gap-2">
                <ShieldCheck size={18} className="text-emerald-600" />
                Thông tin đã gửi
              </h3>
              <div className="mt-3 space-y-2 text-sm text-zinc-700">
                <div><span className="font-semibold">Tên shop:</span> {existingApplication.storeName}</div>
                <div><span className="font-semibold">Liên hệ:</span> {existingApplication.contactEmail} - {existingApplication.phone}</div>
                <div><span className="font-semibold">Danh mục:</span> {existingApplication.category || "-"}</div>
                <div><span className="font-semibold">Mô tả:</span> {existingApplication.description || "-"}</div>
              </div>
              {(existingApplication.avatarUrl || existingApplication.coverUrl) && (
                <div className="mt-3 grid gap-3 sm:grid-cols-2">
                  {existingApplication.avatarUrl && (
                    <div className="space-y-1">
                      <p className="text-xs font-semibold text-zinc-600">Avatar</p>
                      <img src={existingApplication.avatarUrl} alt="Seller avatar" className="h-24 w-24 rounded-full object-cover border" />
                    </div>
                  )}
                  {existingApplication.coverUrl && (
                    <div className="space-y-1">
                      <p className="text-xs font-semibold text-zinc-600">Ảnh bìa</p>
                      <img src={existingApplication.coverUrl} alt="Seller cover" className="h-24 w-full rounded-lg object-cover border" />
                    </div>
                  )}
                </div>
              )}
            </div>
          )}
          <div className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm">
            <h3 className="text-lg font-semibold text-zinc-900 flex items-center gap-2">
              <ShieldCheck size={18} className="text-emerald-600" />
              {t.seller.faq.title}
            </h3>
            <div className="mt-3 space-y-3 text-sm text-zinc-700">
              {t.seller.faq.items.map((item, idx) => (
                <div key={idx} className="rounded-lg border border-zinc-100 bg-zinc-50 p-3">
                  <div className="font-semibold text-zinc-900">{item.q}</div>
                  <div className="text-zinc-600">{item.a}</div>
                </div>
              ))}
            </div>
          </div>
          <div className="rounded-2xl border border-emerald-100 bg-emerald-50 p-5 text-emerald-900">
            <h3 className="text-lg font-semibold flex items-center gap-2">
              <Sparkles size={18} /> Pro tips
            </h3>
            <ul className="mt-3 space-y-2 text-sm">
              <li className="flex items-center gap-2"><ArrowRight size={14} />Use clear product photos & sizing.</li>
              <li className="flex items-center gap-2"><ArrowRight size={14} />Enable COD and fast shipping options.</li>
              <li className="flex items-center gap-2"><ArrowRight size={14} />Respond to chats within 5 minutes.</li>
            </ul>
          </div>
        </div>
      </section>
    </div>
  );
}
