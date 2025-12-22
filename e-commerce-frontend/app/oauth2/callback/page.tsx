'use client';

import { useRouter, useSearchParams } from "next/navigation";
import React, { Suspense, useEffect, useState } from "react";
import { authApi } from "@/src/api/authApi";
import { Spinner } from "@/src/components/ui/spinner";
import { useToast } from "@/src/components/ui/toast-provider";
import { useAuth } from "@/src/store/auth-store";
import { useTranslation } from "@/src/providers/language-provider";

function OAuthCallbackContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { setSessionFromOAuth } = useAuth();
  const { addToast } = useToast();
  const { t } = useTranslation();
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
          throw new Error(t.auth.oauth.missing_tokens);
        }
        addToast(t.auth.oauth.signed_in_success, "success");
        router.replace("/");
      } catch (err) {
        const message = err instanceof Error ? err.message : t.auth.oauth.login_failed;
        addToast(message, "error");
        setStatus("error");
      }
    };

    void complete();
  }, [addToast, router, searchParams, setSessionFromOAuth, t]);

  if (status === "error") {
    return (
      <div className="flex min-h-[60vh] flex-col items-center justify-center">
        <p className="text-lg font-semibold text-black">{t.auth.oauth.failed_title}</p>
        <p className="text-sm text-zinc-600">{t.auth.oauth.failed_desc}</p>
      </div>
    );
  }

  return (
    <div className="flex min-h-[60vh] flex-col items-center justify-center gap-3">
      <Spinner className="h-8 w-8" />
      <p className="text-sm text-zinc-600">{t.auth.oauth.completing}</p>
    </div>
  );
}

export default function OAuthCallbackPage() {
  return (
    <Suspense fallback={
      <div className="flex min-h-[60vh] flex-col items-center justify-center gap-3">
        <Spinner className="h-8 w-8" />
      </div>
    }>
      <OAuthCallbackContent />
    </Suspense>
  );
}
