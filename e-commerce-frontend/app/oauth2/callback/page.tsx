'use client';

import { useRouter, useSearchParams } from "next/navigation";
import React, { Suspense, useEffect, useState } from "react";
import { authApi } from "@/src/api/authApi";
import { Spinner } from "@/src/components/ui/spinner";
import { useToast } from "@/src/components/ui/toast-provider";
import { useAuth } from "@/src/store/auth-store";

function OAuthCallbackContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { setSessionFromOAuth } = useAuth();
  const { addToast } = useToast();
  const [status, setStatus] = useState<"loading" | "error">("loading");

  useEffect(() => {
    const complete = async () => {
      const code = searchParams?.get("code");
      const state = searchParams?.get("state");
      const accessToken = searchParams?.get("accessToken");
      const refreshToken = searchParams?.get("refreshToken");
      const error = searchParams?.get("error");

      if (error) {
        setStatus("error");
        addToast(error, "error");
        return;
      }

      try {
        if (code || state) {
          const response = await authApi.completeSocialLogin({ code: code ?? undefined, state: state ?? undefined });
          await setSessionFromOAuth(response);
        } else if (accessToken && refreshToken) {
          const user = await authApi.me();
          await setSessionFromOAuth({ accessToken, refreshToken, user });
        } else {
          throw new Error("Missing OAuth tokens");
        }
        addToast("Signed in successfully", "success");
        router.replace("/");
      } catch (err) {
        const message = err instanceof Error ? err.message : "OAuth login failed";
        addToast(message, "error");
        setStatus("error");
      }
    };

    void complete();
  }, [addToast, router, searchParams, setSessionFromOAuth]);

  if (status === "error") {
    return (
      <div className="flex min-h-[60vh] flex-col items-center justify-center">
        <p className="text-lg font-semibold text-black">OAuth login failed.</p>
        <p className="text-sm text-zinc-600">Please try again from the login page.</p>
      </div>
    );
  }

  return (
    <div className="flex min-h-[60vh] flex-col items-center justify-center gap-3">
      <Spinner className="h-8 w-8" />
      <p className="text-sm text-zinc-600">Completing sign-in...</p>
    </div>
  );
}

export default function OAuthCallbackPage() {
  return (
    <Suspense fallback={
      <div className="flex min-h-[60vh] flex-col items-center justify-center gap-3">
        <Spinner className="h-8 w-8" />
        <p className="text-sm text-zinc-600">Loading...</p>
      </div>
    }>
      <OAuthCallbackContent />
    </Suspense>
  );
}
