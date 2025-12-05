'use client';

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
      router.replace(next || "/");
    }
  }, [initializing, next, router, user]);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setLoading(true);
    try {
      await login(form);
      addToast(t.auth.logged_in_success, "success");
      router.replace(next || "/");
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
    <div className="mx-auto flex max-w-6xl flex-col gap-8 px-4 py-10 md:flex-row md:items-start">
      <div className="flex-1">
        <h1 className="text-3xl font-bold text-zinc-900">{t.auth.welcome_back}</h1>
        <p className="mt-2 text-sm text-zinc-600">{t.auth.sign_in_subtitle}</p>
        <form className="mt-6 flex flex-col gap-4" onSubmit={handleSubmit}>
          <Input
            label={t.auth.email_label}
            type="email"
            required
            value={form.email}
            onChange={(e) => setForm((prev) => ({ ...prev, email: e.target.value }))}
          />
          <Input
            label={t.auth.password_label}
            type="password"
            required
            value={form.password}
            onChange={(e) => setForm((prev) => ({ ...prev, password: e.target.value }))}
          />
          <Button type="submit" disabled={loading} className="bg-emerald-600 hover:bg-emerald-700">
            {loading ? t.auth.signing_in : t.auth.login_button}
          </Button>
        </form>
        <p className="mt-4 text-sm text-zinc-600">
          {t.auth.no_account}{" "}
          <Link className="font-semibold text-emerald-600 hover:underline" href="/register">
            {t.auth.create_one}
          </Link>
        </p>
      </div>
      <div className="flex-1 rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm">
        <h2 className="text-lg font-semibold text-zinc-900">{t.auth.social_login_title}</h2>
        <p className="mt-2 text-sm text-zinc-600">{t.auth.social_login_subtitle}</p>
        <div className="mt-4 flex flex-col gap-3">
          <Button variant="secondary" onClick={() => socialRedirect("google")}>
            {t.auth.continue_google}
          </Button>
          <Button variant="secondary" onClick={() => socialRedirect("facebook")}>
            {t.auth.continue_facebook}
          </Button>
        </div>
      </div>
    </div>
  );
}

export default function LoginPage() {
  return (
    <Suspense fallback={
      <div className="flex min-h-[60vh] items-center justify-center">
        <Spinner />
      </div>
    }>
      <LoginContent />
    </Suspense>
  );
}
