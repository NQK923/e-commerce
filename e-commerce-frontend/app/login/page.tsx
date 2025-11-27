'use client';

import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import React, { useState } from "react";
import { Button } from "@/src/components/ui/button";
import { Input } from "@/src/components/ui/input";
import { useAuth } from "@/src/store/auth-store";
import { useToast } from "@/src/components/ui/toast-provider";
import { config } from "@/src/config/env";

export default function LoginPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const next = searchParams?.get("next");
  const { login } = useAuth();
  const { addToast } = useToast();
  const [form, setForm] = useState({ email: "", password: "" });
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setLoading(true);
    try {
      await login(form);
      addToast("Logged in successfully", "success");
      router.replace(next || "/");
    } catch (error) {
      const message = error instanceof Error ? error.message : "Login failed";
      addToast(message, "error");
    } finally {
      setLoading(false);
    }
  };

  const socialRedirect = (provider: "google" | "facebook") => {
    const redirectUri = `${config.frontendBaseUrl}/oauth2/callback`;
    window.location.href = `${config.apiBaseUrl}/oauth2/authorization/${provider}?redirect_uri=${encodeURIComponent(redirectUri)}`;
  };

  return (
    <div className="mx-auto flex max-w-6xl flex-col gap-8 px-4 py-10 md:flex-row md:items-start">
      <div className="flex-1">
        <h1 className="text-3xl font-bold text-black">Welcome back</h1>
        <p className="mt-2 text-sm text-zinc-600">Sign in to continue shopping.</p>
        <form className="mt-6 flex flex-col gap-4" onSubmit={handleSubmit}>
          <Input
            label="Email"
            type="email"
            required
            value={form.email}
            onChange={(e) => setForm((prev) => ({ ...prev, email: e.target.value }))}
          />
          <Input
            label="Password"
            type="password"
            required
            value={form.password}
            onChange={(e) => setForm((prev) => ({ ...prev, password: e.target.value }))}
          />
          <Button type="submit" disabled={loading}>
            {loading ? "Signing in..." : "Login"}
          </Button>
        </form>
        <p className="mt-4 text-sm text-zinc-600">
          No account yet?{" "}
          <Link className="font-semibold text-black" href="/register">
            Create one
          </Link>
        </p>
      </div>
      <div className="flex-1 rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm">
        <h2 className="text-lg font-semibold text-black">Social login</h2>
        <p className="mt-2 text-sm text-zinc-600">Continue with your favorite provider.</p>
        <div className="mt-4 flex flex-col gap-3">
          <Button variant="secondary" onClick={() => socialRedirect("google")}>
            Continue with Google
          </Button>
          <Button variant="secondary" onClick={() => socialRedirect("facebook")}>
            Continue with Facebook
          </Button>
        </div>
      </div>
    </div>
  );
}
