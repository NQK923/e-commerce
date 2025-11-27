'use client';

import Link from "next/link";
import { useRouter } from "next/navigation";
import React, { useState } from "react";
import { Button } from "@/src/components/ui/button";
import { Input } from "@/src/components/ui/input";
import { useToast } from "@/src/components/ui/toast-provider";
import { useAuth } from "@/src/store/auth-store";
import { useTranslation } from "@/src/providers/language-provider";

export default function RegisterPage() {
  const router = useRouter();
  const { register } = useAuth();
  const { addToast } = useToast();
  const { t } = useTranslation();
  const [form, setForm] = useState({ email: "", password: "" });
  const [confirmPassword, setConfirmPassword] = useState("");
  const [displayName, setDisplayName] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (form.password !== confirmPassword) {
      addToast(t.auth.passwords_do_not_match, "error");
      return;
    }
    setLoading(true);
    try {
      await register({ email: form.email, password: form.password, displayName: displayName });
      addToast(t.auth.account_created_success, "success");
      router.replace("/");
    } catch (error) {
      const message = error instanceof Error ? error.message : t.common.error;
      addToast(message, "error");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mx-auto flex max-w-3xl flex-col gap-6 px-4 py-10">
      <h1 className="text-3xl font-bold text-zinc-900">{t.auth.create_account_title}</h1>
      <form className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm" onSubmit={handleSubmit}>
        <div className="grid gap-4 md:grid-cols-2">
          <Input
            label={t.auth.display_name_label}
            required
            value={displayName}
            onChange={(e) => setDisplayName(e.target.value)}
          />
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
          <Input
            label={t.auth.confirm_password_label}
            type="password"
            required
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
          />
        </div>
        <Button type="submit" className="mt-6 w-full bg-emerald-600 hover:bg-emerald-700" disabled={loading}>
          {loading ? t.auth.creating_account : t.auth.signup_button}
        </Button>
      </form>
      <p className="text-sm text-zinc-600">
        {t.auth.already_have_account}{" "}
        <Link className="font-semibold text-emerald-600 hover:underline" href="/login">
          {t.auth.login_button}
        </Link>
      </p>
    </div>
  );
}
