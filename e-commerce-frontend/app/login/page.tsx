"use client";

import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import React, { Suspense, useState } from "react";
import { Button } from "@/src/components/ui/button";
import { Input } from "@/src/components/ui/input";
import { useAuth } from "@/src/store/auth-store";
import { useToast } from "@/src/components/ui/toast-provider";
import { config } from "@/src/config/env";
import { useTranslation } from "@/src/providers/language-provider";
import { Spinner } from "@/src/components/ui/spinner";
import { AuthLayout } from "@/src/components/auth/auth-layout";
import { Chrome, Facebook, ArrowRight } from "lucide-react";

function LoginContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const next = searchParams?.get("next");
  const { login, user, initializing } = useAuth();
  const { addToast } = useToast();
  const { t } = useTranslation();
  const [form, setForm] = useState({ email: "", password: "" });
  const [loading, setLoading] = useState(false);

  React.useEffect(() => {
    if (initializing) return;
    if (user) {
      if (user.roles?.includes("ADMIN")) {
        router.replace("/admin");
      } else {
        router.replace(next || "/");
      }
    }
  }, [initializing, next, router, user]);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setLoading(true);
    try {
      await login(form);
      addToast(t.auth.logged_in_success, "success");
    } catch (error) {
      const message = error instanceof Error ? error.message : t.common.error;
      addToast(message, "error");
    } finally {
      setLoading(false);
    }
  };

  const socialRedirect = (provider: "google" | "facebook") => {
    const redirectUri = `${config.frontendBaseUrl}/oauth2/callback`;
    window.location.href = `${config.apiBaseUrl}/oauth2/authorization/${provider}?redirect_uri=${encodeURIComponent(redirectUri)}`;
  };

  if (initializing) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center">
        <Spinner />
      </div>
    );
  }

  if (user) {
    return null;
  }

  return (
    <AuthLayout title={t.auth.welcome_back} subtitle={t.auth.sign_in_subtitle}>
      <form className="space-y-4" onSubmit={handleSubmit}>
        <Input
          label={t.auth.email_label}
          type="email"
          required
          placeholder="name@example.com"
          value={form.email}
          onChange={(e) => setForm((prev) => ({ ...prev, email: e.target.value }))}
          className="h-10"
        />
        <div className="space-y-1">
          <Input
            label={t.auth.password_label}
            type="password"
            required
            placeholder="********"
            value={form.password}
            onChange={(e) => setForm((prev) => ({ ...prev, password: e.target.value }))}
            className="h-10"
          />
          <div className="flex justify-end">
            <Link href="/forgot-password" className="text-xs font-medium text-emerald-600 hover:text-emerald-500">
              Forgot password?
            </Link>
          </div>
        </div>

        <Button
          type="submit"
          disabled={loading}
          className="w-full h-10 bg-emerald-600 hover:bg-emerald-700 text-sm font-semibold shadow-lg shadow-emerald-600/20"
        >
          {loading ? <Spinner size="sm" className="mr-2 text-white" /> : null}
          {loading ? t.auth.signing_in : t.auth.login_button}
          {!loading && <ArrowRight size={16} className="ml-2" />}
        </Button>
      </form>

      <div className="mt-6">
        <div className="relative">
          <div className="absolute inset-0 flex items-center">
            <div className="w-full border-t border-zinc-200" />
          </div>
          <div className="relative flex justify-center text-xs">
            <span className="bg-white px-4 text-zinc-500">{t.auth.social_login_title}</span>
          </div>
        </div>

        <div className="mt-4 grid grid-cols-2 gap-3">
          <Button
            variant="outline"
            onClick={() => socialRedirect("google")}
            className="h-10 text-xs border-zinc-200 hover:bg-zinc-50 hover:text-zinc-900"
          >
            <Chrome size={16} className="mr-2" />
            Google
          </Button>
          <Button
            variant="outline"
            onClick={() => socialRedirect("facebook")}
            className="h-10 text-xs border-zinc-200 hover:bg-zinc-50 hover:text-zinc-900"
          >
            <Facebook size={16} className="mr-2" />
            Facebook
          </Button>
        </div>
      </div>

      <p className="mt-6 text-center text-xs text-zinc-600">
        {t.auth.no_account}{" "}
        <Link href="/register" className="font-semibold text-emerald-600 hover:text-emerald-500 hover:underline transition-all">
          {t.auth.create_one}
        </Link>
      </p>
    </AuthLayout>
  );
}

export default function LoginPage() {
  return (
    <Suspense
      fallback={
        <div className="flex min-h-screen items-center justify-center">
          <Spinner />
        </div>
      }
    >
      <LoginContent />
    </Suspense>
  );
}

