"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import React, { useState } from "react";
import { Button } from "@/src/components/ui/button";
import { Input } from "@/src/components/ui/input";
import { useToast } from "@/src/components/ui/toast-provider";
import { useAuth } from "@/src/store/auth-store";
import { useTranslation } from "@/src/providers/language-provider";
import { AuthLayout } from "@/src/components/auth/auth-layout";
import { Spinner } from "@/src/components/ui/spinner";
import { ArrowRight } from "lucide-react";
import { authApi } from "@/src/api/authApi";

export default function RegisterPage() {
  const router = useRouter();
  const { register } = useAuth();
  const { addToast } = useToast();
  const { t } = useTranslation();
  const [form, setForm] = useState({ email: "", password: "", otpCode: "" });
  const [confirmPassword, setConfirmPassword] = useState("");
  const [displayName, setDisplayName] = useState("");
  const [challengeId, setChallengeId] = useState<string | null>(null);
  const [otpExpiresAt, setOtpExpiresAt] = useState<string | null>(null);
  const [otpSending, setOtpSending] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleSendOtp = async () => {
    if (!form.email) {
      addToast("Please enter your email before requesting an OTP.", "error");
      return;
    }
    setOtpSending(true);
    try {
      const challenge = await authApi.requestOtp(form.email);
      setChallengeId(challenge.id);
      setOtpExpiresAt(challenge.expiresAt);
      addToast("Verification code sent to your email.", "success");
    } catch (error) {
      addToast("Could not send OTP right now. Please try again.", "error");
    } finally {
      setOtpSending(false);
    }
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (form.password !== confirmPassword) {
      addToast(t.auth.passwords_do_not_match, "error");
      return;
    }
    if (!challengeId || !form.otpCode) {
      addToast("Please verify your email with the OTP before continuing.", "error");
      return;
    }
    setLoading(true);
    try {
      await register({
        email: form.email,
        password: form.password,
        displayName: displayName,
        otpCode: form.otpCode,
        challengeId: challengeId,
      });
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
    <AuthLayout 
        title={t.auth.create_account_title} 
        subtitle={t.auth.join_us_subtitle}
    >
      <form className="space-y-4" onSubmit={handleSubmit}>
        <Input
            label={t.auth.display_name_label}
            required
            placeholder="Your Name"
            value={displayName}
            onChange={(e) => setDisplayName(e.target.value)}
            className="h-10"
        />
        <Input
            label={t.auth.email_label}
            type="email"
            required
            placeholder="name@example.com"
            value={form.email}
            onChange={(e) => setForm((prev) => ({ ...prev, email: e.target.value }))}
            className="h-10"
        />
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
            <Input
                label={t.auth.password_label}
                type="password"
                required
                placeholder="********"
                value={form.password}
                onChange={(e) => setForm((prev) => ({ ...prev, password: e.target.value }))}
                className="h-10"
            />
            <Input
                label={t.auth.confirm_password_label}
                type="password"
                required
                placeholder="********"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                className="h-10"
            />
        </div>

        <div className="space-y-2 rounded-lg border border-zinc-200 bg-zinc-50 p-3">
          <div className="flex items-center justify-between text-sm font-medium text-zinc-700">
            <span>Email verification</span>
            <Button type="button" size="sm" variant="secondary" onClick={handleSendOtp} disabled={otpSending}>
              {otpSending ? <Spinner className="mr-2 h-4 w-4" /> : null}
              {otpSending ? "Sending..." : "Send OTP"}
            </Button>
          </div>
          <Input
            label="Enter verification code"
            placeholder="6-digit code"
            value={form.otpCode}
            onChange={(e) => setForm((prev) => ({ ...prev, otpCode: e.target.value }))}
          />
          {otpExpiresAt ? (
            <p className="text-xs text-zinc-500">Code expires at {new Date(otpExpiresAt).toLocaleTimeString()}</p>
          ) : null}
        </div>

        <div className="pt-2">
            <Button 
                type="submit" 
                className="w-full h-10 bg-emerald-600 hover:bg-emerald-700 text-sm font-semibold shadow-lg shadow-emerald-600/20" 
                disabled={loading}
            >
                {loading ? <Spinner size="sm" className="mr-2 text-white" /> : null}
                {loading ? t.auth.creating_account : t.auth.signup_button}
                {!loading && <ArrowRight size={16} className="ml-2" />}
            </Button>
        </div>
      </form>
      
      <p className="mt-6 text-center text-xs text-zinc-600">
        {t.auth.already_have_account}{" "}
        <Link className="font-semibold text-emerald-600 hover:text-emerald-500 hover:underline transition-all" href="/login">
          {t.auth.login_button}
        </Link>
      </p>
    </AuthLayout>
  );
}
