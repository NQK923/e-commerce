'use client';

import Link from "next/link";
import { useRouter } from "next/navigation";
import React, { useState } from "react";
import { Button } from "../../src/components/ui/button";
import { Input } from "../../src/components/ui/input";
import { useToast } from "../../src/components/ui/toast-provider";
import { useAuth } from "../../src/store/auth-store";

export default function RegisterPage() {
  const router = useRouter();
  const { register } = useAuth();
  const { addToast } = useToast();
  const [form, setForm] = useState({ email: "", password: "", confirmPassword: "", displayName: "" });
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (form.password !== form.confirmPassword) {
      addToast("Passwords do not match", "error");
      return;
    }
    setLoading(true);
    try {
      await register({ email: form.email, password: form.password, displayName: form.displayName });
      addToast("Account created", "success");
      router.replace("/");
    } catch (error) {
      const message = error instanceof Error ? error.message : "Registration failed";
      addToast(message, "error");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mx-auto flex max-w-3xl flex-col gap-6 px-4 py-10">
      <h1 className="text-3xl font-bold text-black">Create your account</h1>
      <form className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm" onSubmit={handleSubmit}>
        <div className="grid gap-4 md:grid-cols-2">
          <Input
            label="Display name"
            required
            value={form.displayName}
            onChange={(e) => setForm((prev) => ({ ...prev, displayName: e.target.value }))}
          />
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
          <Input
            label="Confirm password"
            type="password"
            required
            value={form.confirmPassword}
            onChange={(e) => setForm((prev) => ({ ...prev, confirmPassword: e.target.value }))}
          />
        </div>
        <Button type="submit" className="mt-6 w-full" disabled={loading}>
          {loading ? "Creating account..." : "Sign up"}
        </Button>
      </form>
      <p className="text-sm text-zinc-600">
        Already have an account?{" "}
        <Link className="font-semibold text-black" href="/login">
          Log in
        </Link>
      </p>
    </div>
  );
}
