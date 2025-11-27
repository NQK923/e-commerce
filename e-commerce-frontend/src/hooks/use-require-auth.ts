'use client';

import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { useEffect } from "react";
import { useAuth } from "../store/auth-store";

export const useRequireAuth = (redirectTo = "/login") => {
  const { user, initializing } = useAuth();
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();

  useEffect(() => {
    if (initializing) return;
    if (!user) {
      const next = pathname + (searchParams?.toString() ? `?${searchParams.toString()}` : "");
      router.replace(`${redirectTo}?next=${encodeURIComponent(next)}`);
    }
  }, [initializing, pathname, redirectTo, router, searchParams, user]);

  return { user, initializing, isAuthenticated: Boolean(user) };
};
