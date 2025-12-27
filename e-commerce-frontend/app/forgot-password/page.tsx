"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { AuthLayout } from "@/src/components/auth/auth-layout";
import { Input } from "@/src/components/ui/input";
import { Button } from "@/src/components/ui/button";
import { Spinner } from "@/src/components/ui/spinner";
import { useToast } from "@/src/components/ui/toast-provider";
import { authApi } from "@/src/api/authApi";
import { useTranslation } from "@/src/providers/language-provider";

export default function ForgotPasswordPage() {
  const router = useRouter();
  const { addToast } = useToast();
  const { t } = useTranslation();
  const [form, setForm] = useState({ email: "", otpCode: "", newPassword: "" });
  const [confirmPassword, setConfirmPassword] = useState("");
  const [challengeId, setChallengeId] = useState<string | null>(null);
  const [otpExpiresAt, setOtpExpiresAt] = useState<string | null>(null);
  const [otpSending, setOtpSending] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleSendOtp = async () => {
    if (!form.email) {
      addToast(t.auth.enter_email_warning, "error");
      return;
    }
    setOtpSending(true);
    try {
      const challenge = await authApi.forgotPassword(form.email);
      setChallengeId(challenge.id ?? null);
      setOtpExpiresAt(challenge.expiresAt ?? null);
      addToast(t.auth.if_account_exists, "success");
    } catch {
      addToast(t.auth.otp_send_failed, "error");
    } finally {
      setOtpSending(false);
    }
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (form.newPassword !== confirmPassword) {
      addToast(t.auth.passwords_do_not_match, "error");
      return;
    }
    if (!challengeId || !form.otpCode) {
      addToast(t.auth.provide_otp_error, "error");
      return;
    }
    setLoading(true);
    try {
      await authApi.resetPassword({
        email: form.email,
        newPassword: form.newPassword,
        otpCode: form.otpCode,
        challengeId: challengeId,
      });
      addToast(t.auth.password_updated_success, "success");
      router.push("/login");
    } catch (error) {
      const message = error instanceof Error ? error.message : t.auth.reset_failed;
      addToast(message, "error");
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout title={t.auth.reset_password_title} subtitle={t.auth.reset_password_subtitle}>
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

        <div className="space-y-2 rounded-lg border border-zinc-200 bg-zinc-50 p-3">
          <div className="flex items-center justify-between text-sm font-medium text-zinc-700">
            <span>{t.auth.email_verification_label}</span>
            <Button type="button" size="sm" variant="secondary" onClick={handleSendOtp} disabled={otpSending}>
              {otpSending ? <Spinner className="mr-2 h-4 w-4" /> : null}
              {otpSending ? t.auth.sending_otp : t.auth.send_otp}
            </Button>
          </div>
          <Input
            label={t.auth.enter_code_label}
            placeholder="6-digit code"
            value={form.otpCode}
            onChange={(e) => setForm((prev) => ({ ...prev, otpCode: e.target.value }))}
          />
          {otpExpiresAt ? (
            <p className="text-xs text-zinc-500">{t.auth.code_expires_at} {new Date(otpExpiresAt).toLocaleTimeString()}</p>
          ) : null}
        </div>

        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
          <Input
            label={t.auth.new_password_label}
            type="password"
            required
            placeholder="********"
            value={form.newPassword}
            onChange={(e) => setForm((prev) => ({ ...prev, newPassword: e.target.value }))}
            className="h-10"
          />
          <Input
            label={t.auth.confirm_new_password_label}
            type="password"
            required
            placeholder="********"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            className="h-10"
          />
        </div>

        <Button
          type="submit"
          disabled={loading}
          className="w-full h-10 bg-emerald-600 hover:bg-emerald-700 text-sm font-semibold shadow-lg shadow-emerald-600/20"
        >
          {loading ? <Spinner size="sm" className="mr-2 text-white" /> : null}
          {loading ? t.auth.updating : t.auth.reset_password_button}
        </Button>
      </form>

      <p className="mt-6 text-center text-xs text-zinc-600">
        {t.auth.remembered_password}{" "}
        <Link className="font-semibold text-emerald-600 hover:text-emerald-500 hover:underline transition-all" href="/login">
          {t.auth.back_to_login}
        </Link>
      </p>
    </AuthLayout>
  );
}
