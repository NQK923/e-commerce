'use client';

import { useEffect, useMemo } from "react";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { useAuth } from "@/src/store/auth-store";

type ProtectedRouteRule = {
  pattern: RegExp;
};

const PROTECTED_ROUTES: ProtectedRouteRule[] = [
  { pattern: /^\/cart(\/.*)?$/ },
  { pattern: /^\/checkout(\/.*)?$/ },
  { pattern: /^\/orders(\/.*)?$/ },
  { pattern: /^\/profile(\/.*)?$/ },
  { pattern: /^\/seller(\/.*)?$/ },
  { pattern: /^\/admin(\/.*)?$/ },
];

export const ProtectedRouteGuard = () => {
  const { user, initializing } = useAuth();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const router = useRouter();

  const requiresAuth = useMemo(
    () => PROTECTED_ROUTES.some((route) => route.pattern.test(pathname ?? "")),
    [pathname],
  );

  useEffect(() => {
    if (!requiresAuth || initializing) return;
    if (!user) {
      const next = pathname + (searchParams?.toString() ? `?${searchParams.toString()}` : "");
      router.replace(`/login?next=${encodeURIComponent(next)}`);
    }
  }, [initializing, pathname, requiresAuth, router, searchParams, user]);

  return null;
};
